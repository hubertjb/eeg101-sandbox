# -*- coding: utf-8 -*-

import numpy as np
import matplotlib.pylab as plt
from matplotlib.colors import ListedColormap

class GaussianNaiveBayesClassifier():
    """Gaussian Naive Bayes Classifier
    
    HEAVILY based on scikit-learn's implementation: 
    http://scikit-learn.org/stabl/modules/generated/sklearn.naive_bayes.GaussianNB.html#sklearn.naive_bayes.GaussianNB    
    
    Args:
        priors
        
    Attributes:
        class_prior_
        class_count_
        theta_ : np.array of shape [number of classes, number of features] containing the mean of the Gaussians
        sigma_ : np.array of shape [number of classes, number of features] containing the variance of the Gaussians
    
    """
    
    def __init__(self, priors=None):
        
        self.fitted = False
            
    def fit(self, X, y):
        """Fit the Gaussian Naive Bayes model
        
        Args:
            X (np.array) :
            y (np.array) :
        
        Note: Calling `fit()` overwrites previous information. Use `partial_fit()`
            to update the model with new training data.
        """
        
        self.fitted = False # model has already been trained, re-initialize parameters
        self.partial_fit(X, y)
        
    def partial_fit(self, X, y):
        """Fit/Update the Gaussian Naive Bayes Model
        
        Using `partial_fit()` allows to update the model given new data.
        
        Args:
            X (np.array) :
            y (np.array) :
                
        """
        
        if not self.fitted: # model has not been trained yet, initialize parameters
            self.classes_ = np.unique(y)
            self.nb_classes_ = len(self.classes_)
            self.nb_feats_ = X.shape[1]
            
            self.class_count_ = np.zeros((self.nb_classes_, 1))
            self.sum_ = np.zeros((self.nb_classes_, self.nb_feats_))
            self.sumsquares_ = np.zeros((self.nb_classes_, self.nb_feats_))
          
        # Update class prior
        self.class_count_ += np.array([np.sum(y==label) for label in self.classes_]).reshape(-1,1)
        self.class_prior_ = self.class_count_/float(self.class_count_.sum())
        
        # Update sum and mean
        self.sum_ += np.array([np.sum(X[y==label,:], axis=0) for label in self.classes_])
        self.theta_ = self.sum_/self.class_count_
        
        # Update sum of squares and variance
        self.sumsquares_ += np.array([np.sum(X[y==label,:]**2, axis=0) for label in self.classes_])
        self.sigma_ = self.sumsquares_/self.class_count_ - self.theta_**2     
        
        self.fitted = True
    
    def predict(self, X):
            
        return np.argmax(self.predict_proba(X), axis=1)
    
    def predict_proba(self, X):
        
        # Evaluate the posterior for each class one by one
        proba = np.column_stack([np.prod(self._gaussian(X, self.theta_[i,:], self.sigma_[i,:]), axis=1) for i in range(self.nb_classes_)])
        
        return proba/np.sum(proba, axis=1).reshape(-1,1)
    
    def get_params(self):
        return self.class_prior_, self.theta_, self.sigma_
    
    def set_params(self, class_prior=None, theta=None, sigma=None):
        
        if class_prior is not None:
            self.class_prior_ = class_prior

        if theta is not None:  
            self.theta_ = theta
            
        if sigma is not None:
            self.sigma_ = sigma
    
    def score(self, X, y):
        y_hat = self.predict(X)
        return np.mean(y == y_hat) # Accuracy
    
    def _gaussian(self, X, mu, var):
        """Probability of X ~ N(mu,var)
        
        Args:
            X (np.array) : values for which to compute the probability
            mu : mean of the Gaussian distribution
            var : variance of the Gaussian distribution
            
        Returns
            np.array
        """
        
        return np.exp(-(X-mu)**2/(2*var))/(np.sqrt(2*np.pi*var))
        
    def decision_boundary(self, proba=False, limits=None, pts_to_plot=None):
        """Define the decision boundary of the trained classifier.
        
        It should be a piecewise quadratic boundary.
        
        Args:
            limits (list) : limits to plot for each feature
        """
        
        if self.fitted and self.nb_feats_ == 2:
            
            if limits is None:
                # Find plausible intervals based on the fitted model (3 stds around the mean -> 99.7%)
                sd = np.sqrt(self.sigma_)
                limits = [np.min(self.theta_ - 3*sd, axis=0), 
                          np.max(self.theta_ + 3*sd, axis=0)]            

            nb_grid_pts = 500
            x = np.linspace(limits[0][0], limits[1][0], nb_grid_pts)
            y = np.linspace(limits[0][1], limits[1][1], nb_grid_pts)
            xv, yv = np.meshgrid(x, y)
            
            # Get decision for each point
            if proba:
                y_hat = self.predict_proba(np.concatenate((xv.reshape(-1,1), yv.reshape(-1,1)), 1))[:,1]
                cMap = 'YlOrRd'
                colorbar_label = 'P(class=0)'
                colorbar_ticks = np.arange(0,1,10)
            else:       
                y_hat = self.predict(np.concatenate((xv.reshape(-1,1), yv.reshape(-1,1)), 1))
                cMap = self._discrete_cmap(self.nb_classes_, 'cubehelix')
                colorbar_label = 'Class'
                colorbar_ticks = []
            
            z = y_hat.reshape(nb_grid_pts, nb_grid_pts)
                        
            # Plot decision function
            plt.figure()
            plt.subplot(1, 1, 1)
            plt.pcolormesh(x, y, z, cmap=cMap, vmin=np.min(z), vmax=np.max(z))
            plt.title('Decision surface')
            plt.axis([x.min(), x.max(), y.min(), y.max()])
            cbar = plt.colorbar()
            cbar.ax.set_ylabel(colorbar_label)
#            cbar.ax.get_yaxis().set_ticks(colorbar_ticks)
            plt.xlabel('Feature 1')
            plt.ylabel('Feature 2')
            
            if pts_to_plot is not None:
                plt.scatter(pts_to_plot[:,0], pts_to_plot[:,1])
            
        else:
            print('The model should only use 2 features for the decision boundary to be plotted.')
            
    def _discrete_cmap(self, N, base_cmap=None):
        """Create an N-bin discrete colormap from the specified input map.
        
        From https://gist.github.com/jakevdp/91077b0cae40f8f8244a
        """
    
        # Note that if base_cmap is a string or None, you can simply do
        #    return plt.cm.get_cmap(base_cmap, N)
        # The following works for string, None, or a colormap instance:
    
        base = plt.cm.get_cmap(base_cmap)
        color_list = base(np.linspace(0, 1, N))
        cmap_name = base.name + str(N)
        
        return base.from_list(cmap_name, color_list, N)
        

def create_fake_data(nb_features, nb_points, means, variances):
    """
    """
    
    nb_classes = len(variances)
    X_all = []
    y_all = []
    
    for i in range(nb_classes):
        X = np.sqrt(variances[i])*np.random.randn(nb_points, nb_features) + means[i]
        y = np.zeros((X.shape[0],)) + i
        
        X_all.append(X)
        y_all.append(y)
        
    return np.concatenate(X_all, axis=0), np.concatenate(y_all, axis=0)
                          
if __name__ == '__main__':
    
    np.random.seed(42)
    
    # 1. Create fake dataset
    X, y = create_fake_data(2, 200, [-10,10], [5,15])

    # 2. Initialize and train classifier
    clf = GaussianNaiveBayesClassifier()
    clf.fit(X, y)
    
    # 3. Update classifier with new data
    X_new, y_new = create_fake_data(2, 500, [-10,10], [5,5])
    clf.partial_fit(X_new, y_new)
    
    # Test classifier
    X_test, y_test = create_fake_data(2, 10, [-0.9,10], [0.5,0.5])
    
    print(X_test)
    print(clf.predict_proba(X_test))
    print(clf.predict(X_test))
    print(clf.score(X_test, y_test))
    
    # Plot decision boundary
    clf.decision_boundary(proba=True, pts_to_plot=X_test)
    