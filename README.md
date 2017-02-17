# Lint rules for Retrofit 2

There is only one Lint rule at the moment:
* `UnusedCallObject` - detects when Call object is created but never used.
Call adapters that wrap Call are also supported because call detection relies on @GET, @POST, etc. annotations.

# Usage

```
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compile 'com.github.zergtmn:retrofit2-lint:1.0'
}
```

## License

```
Copyright 2017 Sergey Chelombitko

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
