# -*- coding: utf-8 -*-
"""
Testing the Battacharyya distance as a metric of Gaussian overlap
=================================================================

"""

import numpy as np
import matplotlib.pyplot as plt


def gaussian(X, mu, var):
    """Probability of X ~ N(mu,var)

    Args:
        X (np.array) : values for which to compute the probability
        mu (float): mean of the Gaussian distribution
        var (float): variance of the Gaussian distribution

    Returns
        np.array
    """
    return np.exp(-(X-mu)**2/(2*var)) / (np.sqrt(2*np.pi*var))


def battacharyya_distance(mu1, var1, mu2, var2):
    """Battacharyya distance and coefficient between two univariate Gaussians.

    Battacharyya distance and coefficient between two univariate Gaussians.
    coeff = 0 -> no overlap
    coeff = 1 -> complete overlap

    Args:
        mu1
        var2
        mu2
        var2

    Returns:
        (float): Battacharyya distance
        (float): Battacharyya coefficient
    """
    db = np.log((var1/var2 + var2/var1 + 2)/4)/4 + \
        ((mu1 - mu2)**2)/(var1 + var2)/4
    coeff = np.exp(-db)

    return db, coeff


if __name__ == '__main__':

    mu1 = 0.
    var1 = 1.

    mu2 = [0, 1, 5, 10, 100]
    var2 = [0.1, 1, 5, 10, 100]

    x = np.arange(-100, 500, 0.01)

    f, ax = plt.subplots(len(var2), len(mu2))

    for m, mu in enumerate(mu2):
        for v, var in enumerate(var2):
            d, coeff = battacharyya_distance(mu1, var1, mu, var)
            ax[v][m].plot(x, gaussian(x, mu1, var1))
            ax[v][m].plot(x, gaussian(x, mu, var), 'r')
            ax[v][m].set_title('c = {0:.3f}'.format(coeff))
            if v != len(var2) - 1:
                ax[v][m].set_xlabel('')
                ax[v][m].set_xticklabels([])
            if m != 0:
                ax[v][m].set_ylabel('')
                ax[v][m].set_yticklabels([])
