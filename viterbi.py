import math

# Helps visualize the steps of Viterbi.
def print_dptable(V):
  s = "    " + " ".join(("%7d" % i) for i in range(len(V))) + "\n"
  for y in V[0]:
    s += "%.5s: " % y
    s += " ".join("%.7s" % ("%f" % v[y]) for v in V)
    s += "\n"
  print(s)


def viterbi(obs, states, start_p, trans_p, emit_p):
  V = [{}]
  path = {}

  # Initialize base cases (t == 0)
  for y in states:
    V[0][y] = math.log10(start_p[y]) + math.log10(emit_p[y][obs[0]])
    path[y] = [y]

  # alternative Python 2.7+ initialization syntax
  # V = [{y:(start_p[y] * emit_p[y][obs[0]]) for y in states}]
  # path = {y:[y] for y in states}

  print '----'
  print 'T=%s' % (0,)
  print_dptable(V)
  # Run Viterbi for t > 0
  for t in range(1, len(obs)):
    V.append({})
    newpath = {}
    print '----'
    print 'T=%s' % (t,)
    for y in states:
      candidates = [(V[t - 1][y0] +
                     math.log10(trans_p[y0][y]) +
                     math.log10(emit_p[y][obs[t]]), y0)
                    for y0 in states]
      print '  candidates for state %s: %s' % (y, candidates)
      (prob, state) = max(candidates)
      print '  best candidate: %s' % ((prob, state),)
      V[t][y] = prob
      newpath[y] = path[state] + [y]

    # Don't need to remember the old paths
    path = newpath
    print '  path: %s' % (path,)
    print_dptable(V)

  print_dptable(V)
  (prob, state) = max((V[t][y], y) for y in states)
  print 'Final best candidate: %s' % ((prob, state),)
  return (10 ** prob, path[state])


def example():
  states = ('Healthy', 'Fever')
  observations = ('normal', 'cold', 'dizzy')
  start_probability = {'Healthy': 0.6, 'Fever': 0.4}
  transition_probability = {
    'Healthy': {'Healthy': 0.7, 'Fever': 0.3},
    'Fever': {'Healthy': 0.4, 'Fever': 0.6},
  }
  emission_probability = {
    'Healthy': {'normal': 0.5, 'cold': 0.4, 'dizzy': 0.1},
    'Fever': {'normal': 0.1, 'cold': 0.3, 'dizzy': 0.6},
  }
  return viterbi(observations,
                 states,
                 start_probability,
                 transition_probability,
                 emission_probability)

if __name__ == '__main__':
  print(example())
