## Logger
Simple library that utilizes AOP to enable logging of spring-boot components.

## Installation
Logger library is currently stored in [nmg jfrog artifactory](http://jfrog.mynmg.com) under `libs-nmo-dt-local` repository.
Using the gradle artifactory plugin you can download it from artifactory and add it as a dependency to your application as shown below;

1. Add gradle artifactory plugin to your `build.gradle` file
```
    buildscript {
    	dependencies {
    		classpath("org.jfrog.buildinfo:build-info-extractor-gradle:latest.release")
    	}
    }
    
    apply plugin: "com.jfrog.artifactory"
    
    artifactory {
    	contextUrl = project.hasProperty('artifactoryUrl') ? project.artifactoryUrl : ''
    	resolve {
    		repository {
    			repoKey = 'libs-nmo-dt-local'
    			username = project.hasProperty('artifactoryUser') ? project.artifactoryUser : ''
    			password = project.hasProperty('artifactoryPassword') ? project.artifactoryPassword : ''
    			maven = true
    		}
    	}
    }
```

2. Add logger library to a list of dependencies in `build.gradle` file.
```
    dependencies {
        compile('com.nmo:logger:0.0.7-RELEASE')
    }
```
> NOTE: Versions 0.0.6-RELEASE and below had a critical bugs that were in 0.0.7-RELEASE

## Usage
The logger library utilizes AOP to log methods and classes.


### Adding debug logs to methods and classes
We can leverage the `@Loggable` annotation to annotate classes and methods. When the `@Loggable` is applied to a class, 
it will add debug logs to all method in the class.
For example;

```
package com.test

@Loggable
public class Hallo {
    public String sayHello(String name) {
        return "Hallo " + name;
    }
}
```

When the `sayHello("Tim")` is called, log output will be;
```
LOG DEBUG Entering method com.test.Hallo.logSuccessFulEvent(Tim) {}
LOG DEBUG Exiting method com.test.Hallo.logSuccessFulEvent; execution time (ms): 20
```
The debug log will print following;
* Class Name
* Method Name
* Arguments passed to the method
* Execution time in milliseconds

### Adding event logs for monitoring in splunk
To facilitate monitoring in splunk, we need to add event logs to controllers and repositories. We can 
 achieve this by leveraging the `@LoggableEvent`.
 
Controller event logging example;
```
    public class FavoriteController {
    
    	@PostMapping(value = "/addFavorite/{productId}")
    	@LoggableEvent(eventType = "API", action = "ADD_FAVORITE")
    	public ResponseEntity addFavorite(@RequestBody FavoriteOperationRequest request, @PathVariable String productId) {
    		...
    	}
    	...
    }
```
When a request is made to `/addFavorite/{productId}` endpoint and the response is successful, log output will be;
```
LOG INFO event_type="API", action="ADD_FAVORITE", status="Success", duration_millis=20
```
If the method throws a error OR the response status code is a 4XX or 5XX then log output will be;
```
LOG INFO event_type="API", action="ADD_FAVORITE", status="Failed", duration_millis=24
```

Repository event logging example;
```
public class FavoriteRepository {
    
    ...
    
    @LoggableEvent(eventType = "REPOSITORY, action = "GET_FAVORITE_STATUS", hystrixCommandKey=PROFILE_SERVICE_HYSTRIX_KEY)
    @HystrixCommand(groupKey = PROFILE_SERVICE_HYSTRIX_KEY, commandKey = PROFILE_SERVICE_HYSTRIX_KEY, threadPoolKey = PROFILE_SERVICE_HYSTRIX_KEY,
            fallbackMethod = "addFavoriteFallbackResponse")
    public FavoriteOperationResult addFavorite(FavoriteOperationRequest favoriteStatusRequest, String productId) {
        ...    
    }
    
    ...
}

```
When `addFavorite` method in favorite repository is called and the response is successful, log output will be;
```
LOG INFO event_type="REPOSITORY", action="ADD_FAVORITE", status="Success", duration_millis=20
```
If the method throws a error then log output will be;
```
LOG INFO event_type="REPOSITORY", action="ADD_FAVORITE", status="Failed", duration_millis=24
```


#### Logging in hystrix fallback methods
To log in hystrix fallback methods we can leverage the LogEvent builder api.

For example;
```
public class FavoriteRepository {
    
    ...
    
    @SuppressWarnings("unused")
    private FavoriteOperationResult addFavoriteFallbackResponse(FavoriteOperationRequest request, String productId, Throwable cause) throws Throwable {
        String healthStatusCode = hystrixHealthIndicator.health().getStatus().toString();
        
        // Log event builder API
        LogEvent.builder().eventType("FALLBACK").logEventBuilder.action("ADD_FAVORITE").status("Failed").circuitBreakerStatus(healthStatusCode).build().log();
        
        throw cause;
    }
    
    ...
}

```

When the hystrix fallback is called, the log output will be;
```
LOG INFO event_type="FALLBACK", action="ADD_FAVORITE", status="Failure", hystrix="OPEN"
```
