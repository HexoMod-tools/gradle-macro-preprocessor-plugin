# gradle-macro-preprocessor-plugin
[![License: MIT](https://img.shields.io/badge/License-MIT-brightgreen.svg?style=flat-square)](https://opensource.org/licenses/MIT)

A simple macro preprocessor for java

## Supported macros

`//#ifdef`      or      `##ifdef`  
`//#if`         or      `##if`  
`//#elseif`     or      `##elseif`  
`//#else`       or      `##else`  
`//#endif`      or      `##endif`  

# How to use

The preprocessor is published in [Gradle central](https://plugins.gradle.org/plugin/com.github.hexomod.macro.preprocessor).

Using the plugins DSL:
```gradle
plugins {
  id "com.github.hexomod.macro.preprocessor" version "0.9"
}
```

Using legacy plugin application:
```gradle
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "com.github.hexomod:MacroPreprocessor:0.9"
  }
}

apply plugin: "com.github.hexomod.macro.preprocessor"
```

# Usage

```gradle
macroPreprocessorSettings {
    verbose = true      // default: false
    inPlace = false     // default: false
    remove = false      // default: false

    java {
        enable = true       // default: true
        inPlace = true      // default: false
        remove = false      // default: false
    }

    resources {
        enable = true       // default: true
        inPlace = true      // default: true
        remove = true       // default: true
    }
    
    vars = [VAR_STRING: "value_string", VAR_BOOL: true, VAR_INT: 1, VAR_DOUBLE: 2.0, PROJECT: "Basic", DEBUG: true]
}
```

# Examples

- [basic](samples/basic)
- [multi-project1](samples/multi/projects/project1)
- [multi-project2](samples/multi/projects/project2)


# Example of Java sources with directives

```Java
    //#ifdef DEBUG
    public static boolean DEBUG = true; 
    //#else
    ///public static boolean DEBUG = false; 
    //#endif
```


# Example of resources with directives

```yml
##ifdef DEBUG
debug: true
##else
debug: false
##endif
```