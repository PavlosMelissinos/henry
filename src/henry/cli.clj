(ns henry.cli
  (:require [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [clojure.tools.cli :as cli]

            [taoensso.timbre :as log]

            [henry.config :as c]
            [henry.core :as henry]
            [henry.utils :as utils])
  (:gen-class))

(def load-edn utils/load-edn)

(defn file-exists? [f]
  (-> f io/file .exists))

(def cli-options
  [["-m" "--mode MODE" "mode of export"
    :id :mode
    :default "gantt"
    :parse-fn keyword
    :validate [#{:deps :gantt} "Must be either :deps or :gantt"]]
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
  (let [text ["Welcome to henry"
              "A declarative generator of task dependency graphs and gantt charts."
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
              "Please refer to the manual page for more information."]]
    (str/join \newline text)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn target-file [in-file {:keys [mode output-format]}]
  (let [replacement (format ".%s.%s" (name mode) (name output-format))]
    (str/replace in-file #".edn$" replacement)))

(defn validate-args [args]
  (log/info (str "Validating cli arguments: " args))
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)
        spec-file (first arguments)
        spec (if (file-exists? spec-file)
               (-> arguments first load-edn)
               (throw
                 (ex-info (format "File %s does not exist" spec-file)
                          {:options options
                           :spec-file spec-file})))]
    (cond
      (:help options) {:exit-message (usage summary) :ok? true}
      errors          {:exit-message (error-msg errors)}

      (not
        (s/valid? ::c/config spec))
      {:exit-message (s/explain-str ::c/config spec)}

      (= 1 (count arguments))
      (assoc options
        :spec     spec
        :out-file (target-file spec-file options))

      :else           {:exit-message (usage summary)})))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [exit-message ok?] :as ctx}  (validate-args args)
        {:keys [spec mode format out-file]} ctx]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (do (henry/export spec mode format out-file)
          (log/info (str "Done! Check out " out-file))
          (exit 0 nil)))))
