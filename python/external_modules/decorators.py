"""
This module contains some useful decorators
"""

import time
from functools import wraps # to don't have problem with the doc strings

# decorator to measure execution time function
def timeit(func):
        """
        This decorator measure the execution time of the function "func". To use it just
        assign the decorator to the function (@timeit) and call normally the function.
        Another solution is to call the function you want to time as: decorators.timeit(func)(*args)
                            Reference: https://www.andreas-jung.com/contents/a-python-decorator-for-measuring-the-execution-time-of-methods
                                """
        # IMPORTANT!!
        @wraps(func)# wihtout this it gives problem the doc string of the function the decorator is associated with.
        def timed(*args, **kw):
            ts = time.time()
            result = func(*args, **kw)
            te = time.time()
            print '\n%r (%r, %r) %f sec\n' % \
                     (func.__name__, args, kw, te-ts)
            return result
        return timed
