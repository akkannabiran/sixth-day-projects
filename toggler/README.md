## Toggler
Simple library that utilizes AOP to enable toggling of spring-boot components.

## Installation
Toggler library is currently stored in [sixthday jfrog artifactory](http://jfrog.mysixthday.com) under `libs-sixthday-dt-local` repository.
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
    			repoKey = 'libs-sixthday-dt-local'
    			username = project.hasProperty('artifactoryUser') ? project.artifactoryUser : ''
    			password = project.hasProperty('artifactoryPassword') ? project.artifactoryPassword : ''
    			maven = true
    		}
    	}
    }
```

2. Add toggler library to a list of dependencies in `build.gradle` file.
```
    dependencies {
        compile('com.sixthday:toggler:0.0.14-RELEASE')
    }
```

## Usage
The toggler library utilizes AOP to enable and disable features of your application at runtime.
The library expects feature toggles to be defined in your application.yml file under the key `featuretoggles.toggles` 
or in `X-Feature-Toggles` request header.

For example;

- application.yml
```
    featuretoggles:
        toggles:
            FEATURE_ONE: true
            FEATURE_TWO: false
```
- Request-Header: X-Feature-Toggles
```
    X-Feature-Toggles: '{"FEATURE_ONE":true, "FEATURE_TWO":true}'
```
> **Note:** The feature toggles defined in the `X-Feature-Toggles` header take precedence over those defined in application.yml.

### Toggling components. (Recommended)
We can leverage the `@Toggle` annotation to toggle between spring beans. 
For example;

```
// Interface defining shared behavior
public interface Greeting {
    String sayHello(String name);
}


// Fallback component
@Component("hello.german")
public class Hallo implements Greeting {
    @Override
    public String sayHello(String name) {
        return "Hallo " + name;
    }
}


// Main component
@Component("hello.english")
@Toggle(name = "GREET_IN_ENGLISH", fallback = "hello.german")
public class Hello implements Greeting {
    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}

// Calling service
@Service
public class CallingSevice {
    
    @Autowired
    private Hello greeting;  //We are autowiring the implementation not the interface
}
```
> **Note:** This is the recommended method of toggling as it keeps the code maintainable. 
 
### Toggling methods.
We can also use the `@Toggle` annotation to toggle methods.
```
@Component
public class Greeting {

    @Toggle(name = "GREET_IN_ENGLISH", fallback = "sayHallo")
    public String sayHello(String name) {
        return "Hello " + name;
    }

    @SuppressWarnings("unused")
    private String sayHallo(String name) {
        return "Hallo " + name;
    }
}
```
> Note: method annotation will only work if annotated method is called outside the class, for example `new Greeting().sayHello("Tim")`. If the annotated method is called my another method within the class, then it will not work. This limitation is due to the limitations of Spring AOP.

##Testing
The toggler library comes packaged with a JUnit rule that helps developers to enable and disable toggles in unit tests.

Example;
``` 
    public class TestClass {
        
        //JUnit Rules must be public.
        @Rule
        public FeatureToggleRepository featureToggleRepository = new FeatureToggleRepository();
        
        @Test
        public void testThatNeedsToRunWhenFeatureAIsEnabled() {
            featureToggleRepository.enable("FEATURE_A");
            // do something when it's enabled
        }
        
        @Test
        public void testThatNeedsToRunWhenFeatureAIsDisabled() {
            featureToggleRepository.enable("FEATURE_A");
            // do something when it's disabled
        }
    }
```

## Troubleshooting.
Here are some of the more frequently asked questions.

1. **Why aren't my toggles working when I pass the X-Feature-Toggles header?**

The toggler library expects the request context to be available at the point of calling the toggled 
component in order to retrieve the X-Feature-Toggles header. Some of the scenarios where the request 
context may not be available are;
      * Calling the toggled component in a hystrix command without passing the request context to hystrix
      * Calling the toggled component in an async future without passing a request aware task executor





