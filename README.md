# [PDSP](https://doi.org/XXXXX)
Power Dominating Set Problem. Under review, README will be updated soon after publication.

## Abstract
Paper abstract or summary of the article submitted.

## Authors
Authors involved in this work and their respective contributions:
- Person1.
- Person2.
- 
## Datasets


### Instance format

(Explain instance format so other users may easily use them even if not using your code.)


## Compiling

You can easily compile and build an executable artifact of this project using Maven and a recent version of Java (17+):
```text
mvn clean package
```

## Executing

Executable artifacts are generated inside the `target` directory. For ease of use, there is an already executable JAR inside the target folder.
To review a full list of configurable parameters, see the `application.yml`, or review the [configuration section of the Mork documentation](https://docs.mork-optimization.com/en/latest/features/config/).

Example 1: execute all experiments with the default set of instances
```text
java -jar target/PDSP-0.19-SNAPSHOT.jar 
```

Example: execute `Experiment2` using a different set of instances, located inside the `newinstances` folder.
```
java -jar target/PDSP-0.19-SNAPSHOT.jar --instances.path.default=newinstances --solver.experiment=Experiment2
```

All experiments are declared inside the `src/main/java/es/urjc/etsii/grafo/PDSP/experiments` folder. New experiments can be easily added and invoked as necessary.

## Cite

Consider citing our paper if used in your own work:
(Fill with the references to your own published work)

### DOI
https://doi.org/XXXXXXX

### Bibtex
```bibtex
@article{
...
}
```

## Powered by MORK (Metaheuristic Optimization framewoRK)
| ![Mork logo](https://user-images.githubusercontent.com/55482385/233611563-4f5c91f2-af36-4437-a4b5-572b6655487a.svg) | Mork is a Java framework for easily solving hard optimization problems. You can [create a project](https://generator.mork-optimization.com/) and try the framework in under one minute. See the [documentation](https://docs.mork-optimization.com/en/latest/) or the [source code](https://github.com/mork-optimization/mork). |
|--|--|
