# clj-viterbi

[![Build Status](https://travis-ci.org/wiseman/clj-viterbi.png?branch=master)](https://travis-ci.org/wiseman/clj-viterbi)

Viterbi decoding in clojure.

[![Version](https://clojars.org/com.lemonodor.viterbi/latest-version.svg)](https://clojars.org/com.lemonodor.viterbi)

Viterbi decoding finds the most likely path through a hidden Markov
model (HMM) that generates a series of observations.

## Example

Here's an HMM that describes a model of health & sickness, with symptoms:

![Screenshot of Mavelous running in a desktop
browser](https://github.com/wiseman/clj-viterbi/raw/master/doc/example-hmm.png
"Mavelous in a desktop browser")

According to this model, there is a 60% chance of starting out healthy
and a 40% chance of starting out sick.  Then on any given day, if
someone is healthy then they have a 70% of staying healthy the next
day and a 30% chance of getting a fever.  If someone has a fever, they
have a 60% of continuing to have a fever the next day, or a 40% chance
of recovering.

If someone is healthy, there is a 10% chance of them being dizzy, a
40% chance of them being cold, and a 50% chance of being normal.  If
someone has a fever, there is a 60% chance of being dizzy, a 30%
chance of being cold, and a 10% chance of being normal.

To define an HMM, you call `make-hmm` with the following keyword arguments:

|Argument  | Description                                                                       |
|----------|-----------------------------------------------------------------------------------|
|`:states` | The possible states.                                                              |
|`:start-p`| A map from state → starting probability of the state.                            |
|`:trans-p`| A function F(_s<sub>1</sub>_, _s<sub>2</sub>_) → probability of transitioning from state _s<sub>1</sub>_ to state _s<sub>2</sub>_. |
|`:emit-p` | A function F(_s_, _o_) → probability of emitting observation _o_ from state _s_. |

For example,

```
(ns viterbi-example
    (:require [com.lemonodor.viterbi :as viterbi]))

(def hmm
  (viterbi/make-hmm
   :states [:healthy :fever]
   :start-p {:healthy 0.6, :fever 0.4}
   :trans-p (let [t {:healthy {:healthy 0.7, :fever 0.3},
                     :fever {:healthy 0.4, :fever 0.6}}]
              #((t %1) %2))
   :emit-p (let [t {:healthy {:normal 0.5, :cold 0.4, :dizzy 0.1},
                    :fever {:normal 0.1, :cold 0.3, :dizzy 0.6}}]
             #((t %1) %2)))
```

Now we can find the sequence of states that is most likely given the
sequence of observations normal, cold, dizzy:

```
(viterbi/viterbi hmm [:normal :cold :dizzy])
;; -> [-1.8204482088348124 [:healthy :healthy :fever]]
```

The return value is a vector containing the
log<sub10</sub>-probability of the most likely sequence of states, and
the most likely sequence of states.  In this case, the most likely
sequence of states is healthy, healthy, fever.
