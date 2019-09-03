(ns clj-contentful.util)

(defn deep-merge [& args]
  (if (every? #(or (map? %) (nil? %)) args)
    (apply merge-with deep-merge args)
    (last args)))

(defmacro defn-wrap
  "Like defn, but applies wrap-fn."
  [name-sym wrap-fn & body]
  `(do
     (defn ~name-sym ~@body)
     (alter-var-root #'~name-sym ~wrap-fn)))
