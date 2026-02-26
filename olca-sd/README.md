# olca-sd

This is an interpreter for system dynamics (SD) models. The idea is that SD models can be directly loaded and executed in openLCA. The parameters of an SD model can be then linked to parameters of one or more linked openLCA product systems. The combined model can be then executed in a simulation. In each simulation step, it passes the calculated values of SD variables (stocks, rates, auxiliaries) into the LCA models which are then calculated with these parameter values. It will then produce a sequence of LCA results for the respective simulation steps.


## Status

This is a first step to explore a possible SD integration in openLCA. We will support loading and linking SD models from [XMILE](https://docs.oasis-open.org/xmile/xmile/v1.0/os/xmile-v1.0-os.html) files (see below) but not editing them in openLCA in this step. Also, these models, their product system links and simulation setup is currently stored outside of the openLCA database. Graphical editing and a better openLCA integration of SD models could be the next step.


### openLCA integration

The development is done in the `sd-sim` branch.

+ [x] Manage and run SD models
+ [x] Link product systems and bind parameters
+ [x] Run coupled simulations
+ [x] Result visualization of SD variables and LCIA results
+ [x] Excel export of results
+ [ ] Link the quantitative reference of a product system to a SD variable


### Supported XMILE features

We try to follow the [XMILE standard](https://docs.oasis-open.org/xmile/xmile/v1.0/os/xmile-v1.0-os.html) but with the aim to get [SD models](https://exchange.iseesystems.com/directory/isee) created with other tools like Stella into openLCA. For example, Stella seems to use the tag name `dimensions` instead of `dims` as defined in the standard for array dimensions, so we also use that tag name.

+ Basic functionality
  + [x] Stocks
  + [x] Flows
  + [x] Auxiliaries
  + [x] Graphical functions in flows and auxiliaries
  + [ ] Stand-alone graphical functions
  + [ ] Groups
  + [ ] Multiple models
  + [ ] Inclusions

+ Mathematical functions
  + [x] ABS
  + [x] ARCCOS
  + [x] ARCSIN
  + [x] ARCTAN
  + [x] COS
  + [x] EXP
  + [x] INF
  + [x] INT
  + [x] LN
  + [x] LOG10
  + [x] MAX
  + [x] MIN
  + [x] PI
  + [x] SIN
  + [x] SQRT
  + [x] TAN

+ Statistical functions
  + [x] EXPRND
  + [x] LOGNORMAL
  + [x] NORMAL
  + [x] POISSON
  + [x] RANDOM

+ Delay functions
  + [ ] DELAY
  + [ ] DELAY1
  + [ ] DELAY3
  + [ ] DELAYN
  + [ ] FORCST
  + [ ] SMTH1
  + [ ] SMTH3
  + [ ] SMTHN
  + [ ] TREND

+ Test input functions
  + [ ] PULSE
  + [ ] RAMP
  + [ ] STEP

+ Time functions
  + [x] DT
  + [x] STARTTIME
  + [x] STOPTIME
  + [x] TIME

+ Miscellaneous functions
  + [x] INIT
  + [ ] PREVIOUS
  + [ ] SELF

+ Arrays
  + [x] N dimensional arrays
  + [x] Arithmetic operations
  + [ ] Transposition operator
  + [ ] Slicing over dimensions in assignments
  + [ ] Dimension position operator

+ Array built-in functions
  + [ ] MAX
  + [ ] MEAN
  + [ ] MIN
  + [ ] RANK
  + [ ] SIZE
  + [ ] STDDEV
  + [ ] SUM

+ Integration methods
  + [x] Euler's method
  + [ ] Runge-Kutta 4
  + [ ] Runge-Kutta 2
  + [ ] Runge-Kutta mixed 4th-5th-order
  + [ ] Gear algorithm

+ Other features
  + [ ] Global behavior (e.g. all stocks non-negative)
  + [ ] Unlabeled dimensions
  + [ ] Simulation events
  + [ ] Macros
  + [ ] Conveyors
  + [ ] Queues
  + [ ] Submodels
  + [ ] Unit checking
