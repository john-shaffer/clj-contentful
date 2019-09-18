# clj-contentful

A [Clojure](http://clojure.org) library for the [Contentful APIs](https://www.contentful.com/developers/docs/concepts/apis/).

## Install

Add the following dependency to your `project.clj` file:

```clojure
[clj-contentful "1.0.0"]
```
    
## Usage

Provide the configuration as `[space-id access-token environment]`. `environment` is optional and defaults to `"master"`.

```clojure
(def config [space-id access-token "master"])
```

### Examples

```clojure
(use 'clj-contentful)

; Gets a space
(get-space config)

; Gets the content model of a space
(content-types config)

; Gets a single content type
(content-type config content-type-id)

; Gets all entries of a space.
(entries config)

; Gets a single entry.
(entry config entry-id)

; Gets all assets of a space.
(assets config)

; Gets a single asset.
(asset config asset-id)

; Gets all locales of a space.
(locales config)
```

Queries and link inclusion options are specified as query parameters to the `entries` function. Their usage is described in the [Content Delivery API](https://www.contentful.com/developers/docs/references/content-delivery-api/).

```clojure
; Request only the titles of posts.
(entries config {:content_type "post" :select "fields.title"})

; Gets all posts with a title of "Hi".
(entries config {:content_type "post" :fields.title "Hi"})

; Include 2 levels of links
; https://www.contentful.com/developers/docs/references/content-delivery-api/#/reference/links/retrieval-of-linked-items
(entries config {:include 2})
```

It is often more convenient to specify the config once via the `with-config` macro. It should then not be passed as an argument to clj-contentful functions.

```clojure
(with-config config
    (let [sp (space)
          xs (entries)]
      ; Perform some action.
      ))
````

`with-config` uses [thread-binding](https://clojure.org/reference/vars#conveyance). You may nest one `with-config` inside another without issue. Clojure forms like functions and agents will automatically convey the bindings across threads. If you create threads through Java interop, those threads will have to specify the config anew.

If you need to return to specifying config as an argument, you may use `(with-config nil ...body...)` to clear its effects.

## License

Copyright © 2019 John Shaffer

Distributed under the MIT License.
