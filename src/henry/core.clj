(ns henry.core
  (:require [clojure.string :as str]
            [clojure.tools.cli :as cli]

            [henry.gantt :as gantt]
            [henry.tangle :as deps]
            [henry.utils :as utils]
            [clojure.java.io :as io]
            [clojure.string :as string])
  (:gen-class))

(def load-edn utils/load-edn)

(defn- deps
  "Generates a task dependency graph from the input spec into the specified format.
   Currently supports the png and dot formats."
  ([spec]
   (deps spec :dot))
  ([spec format]
  (condp = (keyword format)
    :png (deps/png spec)
    :dot (deps/dot spec)
    (throw (ex-info
             (format "Invalid format %s"
                     (str/upper-case (name format)))
             {:fn :deps
              :type type})))))

(defn- gantt
  "Generates a gantt chart from the input spec into the specified format.
   Currently supports the svg format."
  [spec format]
  (condp = (keyword format)
    :svg  (-> spec gantt/vega-lite-spec gantt/svg)
    (throw (ex-info
             (format "Invalid format %s"
                     (str/upper-case (name format)))
             {:fn :gantt
              :format format}))))

(defn convert [spec mode format]
  (condp = mode
    :deps  (deps spec format)
    :gantt (gantt spec format)))

(defn export [{:keys [spec mode format out-file] :as ctx}]
  (condp = (keyword mode)
    :deps  (-> spec deps/dot (deps/export out-file))
    :gantt (-> spec gantt/vega-lite-spec (gantt/export format out-file))
    (throw (ex-info
             (format "Invalid mode %s"
                     (str/upper-case (name mode)))
             {:fn :export
              :mode mode}))))

(defn file-exists? [f]
  (-> f io/file .exists))

(def cli-options
  ;; An option with a required argument
  [["-m" "--mode MODE" "mode of export"
    :id :mode
    :default "gantt"
    :parse-fn keyword
    :validate [#{:deps :gantt} "Must be either :deps or :gantt"]]
   ["-i" "--input-file INFILE" nil "Input file"
    :id :in-file
    :validate [file-exists? "File does not exist"]]
   ["-o" "--output-file OUTFILE" nil "Output file"
    :id :out-file]
   ["-f" "--format FORMAT" "Output format"
    :id :output-format
    :parse-fn keyword
    :assoc-fn (fn [m _ v] (assoc m :format v
                                   :output-format v))
    :default "png"]
   ["-h" "--help"]])


(defn usage [options-summary]
  (->> ["Welcome to the CLI of henry, a declarative generator of task dependency graphs and gantt charts."
        ""
        "Usage: program-name [options]"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  start    Start a new server"
        "  stop     Stop an existing server"
        "  status   Print a server's status"
        ""
        "Please refer to the manual page for more information."]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))


(defn validate-args [args]
  (let [{:keys [options errors summary]}     (cli/parse-opts args cli-options)
        {:keys [in-file mode output-format]} options]
    (cond
      (:help options) {:exit-message (usage summary) :ok? true}
      errors          {:exit-message (error-msg errors)}
      (not in-file)   (throw (ex-info "Missing required argument -i" nil))
      :else           (assoc options
                        :spec     (load-edn in-file)
                        :out-file (clojure.string/replace
                                    in-file
                                    #".edn$"
                                    (format
                                      ".%s.%s"
                                      (name mode)
                                      (name output-format)))))))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys[exit-message ok?] :as ctx} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (do (export ctx)
          (exit 0 nil)))))
