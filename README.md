# gradle-macro-preprocessor-plugin
[![License: MIT](https://img.shields.io/badge/License-MIT-brightgreen.svg?style=flat-square)](https://opensource.org/licenses/MIT)

A simple macro preprocessor for java

## Supported macros

`#ifdef`  
`#if`  
`#else`  
`#endif`  

# How to use

The preprocessor is published in Gradle central
```gradle
plugin {
    id: 'com.github.hexomod.macro.preprocessor'
}
```

# Usage

```gradle
macroPreprocessorSettings {
    verbose true

    // Default sourcset is automaticaly used
    //source = [ "src/api/java", "src/main/java" ]
    //resources = [ "src/api/resources", "src/main/resources" ]

    // Default output
    //target = "build/preprocessor/macro"

    vars = [ VAR_STRING: "value_string", VAR_BOOL: true, VAR_INT: 1, VAR_DOUBLE: 1.0, DEBUG: true]
}
```

# Internal test samples
- [basic](samples/basic)
- [multi-project1](samples/multi/projects/project1)
- [multi-project2](samples/multi/projects/project2)

# Example of Java sources with directives
In Java the only allowed way to inject directives and to not break work of tools and conpilers - is to use commented space, so that the preprocessor uses it.
```Java
    //#ifdef DEBUG
    public static boolean DEBUG = true; 
    //#else
    //#public static boolean DEBUG = false; 
    //#endif
```
