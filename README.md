#Utils Magic

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
	        implementation 'com.github.Shahidzbi4213:Magic-Utils:1.0.0'
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
