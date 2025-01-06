# [PDSP](https://doi.org/XXXXX)
Power Dominating Set Problem. Under review, README will be updated soon after publication.


[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.10015214.svg)](https://doi.org/10.5281/zenodo.10015214)

## Abstract
We propose a reference framework to apply the multiparent path relinking (MPR) methodology.
MPR is an extension of path relinking (PR) that has been proposed and described but,
to the best of our knowledge, it has never been applied. PR is a trajectory-based neighborhood search strategy
that explores paths that traverse pairs of solutions. PR has been widely used for search intensification
purposes within metaheuristic implementations. To show how MPR can be embedded in more than one setting, our proposal
consists of both employing MPR as a post-processing step in a greedy randomized adaptive search procedure (GRASP) and
as the combination method in a scatter search (SS). We use the power dominating set problem (PDSP) as our testing
platform because its difficulty and structure enable the design and assessment of various strategies. The PDSP seeks
to find the minimum placement of measurement devices in an electrical network to monitor the entire system. Our 
computational experiments are designed to identify the contribution of each element of our proposed MRP implementations.

## Authors
Authors involved in this work and their respective contributions:
- Raúl Martín Santamaría
- Anna Martínez-Gavara
- Ana Dolores López-Sánchez
- Manuel Laguna


## Instance format
There are different instance formats that are frequently used in this family of problems. The most common are explained in the next subsections.
For more details, see the [PDSPInstanceImporter](https://github.com/rmartinsanta/PDSP/blob/main/src/main/java/es/urjc/etsii/grafo/PDSP/model/PDSPInstanceImporter.java) class, where the actual loading process is implemented.

Example graph that will be explained in different formats:
<p align="left">
  <img src="https://github.com/user-attachments/assets/a443fbb9-fdd8-47b7-ab0b-7a36b91c6af5" />
</p>


### IEE format
Files ending in `.graph` are loaded as loaded using the IEE parser. The file starts with a line containing the number of nodes (N) and the number of edges (E).
In the next E lines, in each line we have the edge definition, containing the two nodes that are connected by this edge. 
Note that IEE format is one indexed, nodes start counting at 1!

Example:
```IEE
3 2
1 2
2 3
```
In this instance, we have 3 nodes and two edges. Nodes 1 and 2, and 2 and 3 are connected, but nodes 1 and 3 are not directly connected.

### Adjacency matrix
First number in file (N) represents the number of nodes in the instance. Then, the next N x N numbers contain the weight of the edge, 1 means connected, 0 not connected.
Example:
```Adjacency matrix
3
0 1 0
1 0 1
0 1 0
```
Note that the matrix must be symmetric, as the graph is not directed.

### PTXT format
A very verbose text based format that we do not recommend using. Example using the same graph:
```ptxt
Number Of Vertices
3
Edges
Number edges containting node 0
1
list
1
Number edges containting node 1
2
list
0
2
Number edges containting node 2
1
list
1
```
Note that this format is 0 indexed.

## Executing (Recommended)
Use Docker to easily create a Java environment with all dependencies installed. You can use the following commands to run the project:

```bash
# Build container
docker build -f docker/Dockerfile -t rmartinsanta/pdsp .
# Execute container mapping the results folder to easily retrieve results
# If using Windows, use powershell and replace $(pwd) with $PWD
docker run --rm --mount type=bind,src=$(pwd)/results,target=/results rmartinsanta/pdsp
```

## Compiling manually 
Use a Java LTS version such as Java 21 to compile and run the project. Requires [Maven](https://maven.apache.org/install.html). Example:
```bash
mvn clean package
```

Will create the executable JAR file inside the `target` folder.

## Executing manually

Executable artifacts are generated inside the `target` directory.
To review a full list of configurable parameters, see the `application.yml`, or review the [configuration section of the Mork documentation](https://docs.mork-optimization.com/en/latest/features/config/).

Example 1: execute all experiments with the default set of instances
```text
java -jar target/PDSP-0.19.jar 
```

Example: execute `Experiment2` using a different set of instances, located inside the `newinstances` folder.
```
java -jar target/PDSP-0.19.jar --instances.path.default=newinstances --solver.experiment=Experiment2
```

All experiments are declared inside the `src/main/java/es/urjc/etsii/grafo/PDSP/experiments` folder.
New experiments can be easily added and invoked as necessary.

## Cite

Consider citing our paper if used in your own work:
Waiting for publication

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
