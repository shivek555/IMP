import numpy as np
from sklearn.cluster import KMeans
from scipy.spatial import distance_matrix
from random import shuffle
from math import exp

# Sample data: coordinates of bus stops
bus_stops = np.array([
    [37.7749, -122.4194],  
    [37.8049, -122.4124],
    [37.7849, -122.4094],
    [37.7949, -122.4144],
    [37.7649, -122.4294],
    [37.7849, -122.4194],
    [37.7949, -122.4244],
])

# Parameters
num_clusters = 3  
max_iterations = 100
initial_temp = 1000
final_temp = 1
alpha = 0.995
# Step 1: Clustering using K-means
kmeans = KMeans(n_clusters=num_clusters, random_state=0).fit(bus_stops)
labels = kmeans.labels_

def calculate_total_distance(route, dist_matrix):
    """Calculate the total distance of the given route."""
    return sum(dist_matrix[route[i], route[i + 1]] for i in range(len(route) - 1)) + dist_matrix[route[-1], route[0]]

def simulated_annealing(cluster_coords, max_iter, initial_temp, final_temp, alpha):
    """Perform simulated annealing to find the best route."""
    dist_matrix = distance_matrix(cluster_coords, cluster_coords)
    num_stops = len(cluster_coords)
    current_route = list(range(num_stops))
    shuffle(current_route)
    current_cost = calculate_total_distance(current_route, dist_matrix)
    
    best_route = current_route.copy()
    best_cost = current_cost
    
    temp = initial_temp
    
    for _ in range(max_iter):
        new_route = current_route.copy()
        i, j = np.random.randint(0, num_stops, size=2)
        new_route[i], new_route[j] = new_route[j], new_route[i]
        new_cost = calculate_total_distance(new_route, dist_matrix)
        
        if new_cost < best_cost:
            best_route, best_cost = new_route, new_cost
        elif exp((current_cost - new_cost) / temp) > np.random.rand():
            current_route, current_cost = new_route, new_cost
        
        temp = max(final_temp, temp * alpha)
    
    return best_route, best_cost

# Iterate over each cluster and optimize routes
cluster_routes = []
for cluster_id in range(num_clusters):
    cluster_coords = bus_stops[labels == cluster_id]
    best_route, best_cost = simulated_annealing(cluster_coords, max_iterations, initial_temp, final_temp, alpha)
    cluster_routes.append((best_route, best_cost))

# Print results
for idx, (route, cost) in enumerate(cluster_routes):
    print(f"Cluster {idx + 1} route:")
    for stop in route:
        print(bus_stops[stop])
    print(f"Total distance: {cost:.2f}")
    print()