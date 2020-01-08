# Calculate WordPress usage worldwide

The following script will analyze the list of the first million domains with the most visits to give you the percentage of use.

Warning that it can take a long time: between 20 to 30 days.

## Requirements

To run it you'll need either 2Gb of RAM or a swap file of the same size.

## Install

``` sh
sudo apt install clojure leiningen wget make curl bash unzip
```

## Prepare

``` sh
make prepare
```

## Run

``` sh
lein run
```

When all the CSV sites are analyzed, you can see the final figure by running the following script

## Calculate percentage

``` sh
bash calculate-percentage.sh
```

## Historical

### 2019

19%

### 2020

29%

## Article with conclusions (in Spanish)

https://programadorwebvalencia.com/analizando-un-millon-de-paginas-para-saber-cuanto-se-usa-wordpress-2019/

## Hacker News (comments)

https://news.ycombinator.com/item?id=21428149
