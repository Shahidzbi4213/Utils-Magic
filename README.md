# Utils Magic

Android Utilities Library build in kotlin Provide user 100 of pre defined method to create advanced native android app.

# Gradle

> Step 1. Add the Magic Utils to your build file

```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  ```
 > Step 2. Add the dependency
 
 ```
 dependencies {
	         implementation 'com.github.Shahidzbi4213:Utils-Magic:1.0.0'
	}
  ```
 
 # Usage
 > Find Address Based on Coordinates
 
 ```
  val list  = Utilities.AreaElement.getAddress(this,lattitude, longitude)
 ```

> Get Exact File Path based on Uri

```
val path =  Utilities.PathFinder.getFilePath(this,uri)

```

> Other Useful Functions 

```
val list =  Utilities.Countries.countriesList()

val token_64 =  Utilities.Random.generateToken()

val extentions = Utilities.FileUtility.fileExtension(uri, context)

```

# License
```
Copyright 2021 Square, Inc.

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
