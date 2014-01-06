(require 'clojure.java.io)
(alter-env! assoc
  :joodo.root.namespace "guessing_game"
  :site-root "http://localhost:3000"
  :datastore (with-open [r (clojure.java.io/reader "config/datastore.clj")] (read (java.io.PushbackReader. r)))
)
