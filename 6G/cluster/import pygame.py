import numpy as np
from sklearn.cluster import KMeans
import random
import math
import folium
from folium.plugins import AntPath

# Define approximate locations for Ambala Cantt, Chandigarh, Patiala, Chitkara University
coordinates = {
    "Ambala Cantt": [30.3782, 76.7767],
    "Chandigarh": [30.7333, 76.7794],
    "Patiala": [30.3362, 76.3922],
    "Chitkara University": [30.5138, 76.6595]
}

# Generate random stops near these locations to simulate actual stops
np.random.seed(42)
num_stops_per_city = 12  # 36 stops in total across Ambala, Chandigarh, Patiala (3 clusters per city)
locations = np.vstack([
    np.random.uniform(low=[30.36, 76.7], high=[30.39, 76.8], size=(num_stops_per_city, 2)),  # Ambala
    np.random.uniform(low=[30.71, 76.74], high=[30.75, 76.8], size=(num_stops_per_city, 2)),  # Chandigarh
    np.random.uniform(low=[30.31, 76.35], high=[30.35, 76.45], size=(num_stops_per_city, 2))  # Patiala
])

# Append the destination (Chitkara University) to the locations separately
chitkara_university = np.array(coordinates["Chitkara University"]).reshape(1, -1)

# Perform K-Means clustering on the 36 stops into 12 clusters (3 stops per cluster)
num_clusters = 12  # 12 clusters in total
kmeans = KMeans(n_clusters=num_clusters, n_init=10)
clusters = kmeans.fit_predict(locations)

# Haversine formula to calculate the distance between two points on the earth
def haversine_distance(p1, p2):
    R = 6371  # Earth radius in kilometers
    lat1, lon1 = np.radians(p1[0]), np.radians(p1[1])
    lat2, lon2 = np.radians(p2[0]), np.radians(p2[1])

    dlat = lat2 - lat1
    dlon = lon2 - lon1

    a = np.sin(dlat / 2)**2 + np.cos(lat1) * np.cos(lat2) * np.sin(dlon / 2)**2
    c = 2 * np.arctan2(np.sqrt(a), np.sqrt(1 - a))

    return R * c  # distance in kilometers

# Total route distance using the Haversine formula
def total_route_distance(route, locations):
    total_dist = 0
    for i in range(len(route) - 1):
        total_dist += haversine_distance(locations[route[i]], locations[route[i+1]])
    return total_dist

# Genetic Algorithm Parameters
population_size = 100
mutation_rate = 0.1
num_generations = 500

# Generate initial population (random routes)
def generate_population(size, n):
    population = []
    for _ in range(size):
        individual = list(np.random.permutation(n))
        population.append(individual)
    return population

# Fitness function (minimizing the distance)
def fitness(individual, locations):
    return total_route_distance(individual, locations)

# Selection (Tournament Selection)
def tournament_selection(population, fitnesses, k=3):
    selected = random.sample(list(zip(population, fitnesses)), k)
    selected = sorted(selected, key=lambda x: x[1])
    return selected[0][0]

# Crossover (Ordered Crossover)
def ordered_crossover(parent1, parent2):
    size = len(parent1)
    start, end = sorted(random.sample(range(size), 2))
    child = [-1] * size
    child[start:end] = parent1[start:end]
    pointer = end
    for i in range(size):
        if parent2[(i + end) % size] not in child:
            child[pointer % size] = parent2[(i + end) % size]
            pointer += 1
    return child

# Mutation (Swap mutation)
def mutate(individual):
    if random.random() < mutation_rate:
        i, j = random.sample(range(len(individual)), 2)
        individual[i], individual[j] = individual[j], individual[i]
    return individual

# Genetic Algorithm to find the best route
def genetic_algorithm(locations, population_size, num_generations):
    n = len(locations)
    population = generate_population(population_size, n)
    
    for generation in range(num_generations):
        fitnesses = [fitness(individual, locations) for individual in population]
        
        new_population = []
        for _ in range(population_size):
            parent1 = tournament_selection(population, fitnesses)
            parent2 = tournament_selection(population, fitnesses)
            child = ordered_crossover(parent1, parent2)
            child = mutate(child)
            new_population.append(child)
        
        population = new_population
    
    fitnesses = [fitness(individual, locations) for individual in population]
    best_route = population[np.argmin(fitnesses)]
    best_distance = min(fitnesses)
    
    return best_route, best_distance

# Define colors for 12 clusters
cluster_colors = [
    'blue', 'green', 'red', 'purple', 'orange', 'darkred', 
    'cadetblue', 'darkgreen', 'lightgray', 'pink', 'darkblue', 'lightgreen'
]

# Create the folium map
m = folium.Map(location=[np.mean(locations[:, 0]), np.mean(locations[:, 1])], zoom_start=11)

# Fuel cost parameters
fuel_cost_per_unit = 10

# Apply GA to each cluster and plot routes
for i in range(num_clusters):
    # Get the stops in the cluster
    cluster_locs = locations[clusters == i]
    
    # Include Chitkara University in the route
    cluster_with_destination = np.vstack([cluster_locs, chitkara_university])
    
    # Run Genetic Algorithm to find the best route
    best_route, best_distance = genetic_algorithm(cluster_with_destination, population_size, num_generations)
    
    # Calculate the total fuel cost
    total_fuel_cost = best_distance * fuel_cost_per_unit
    
    # Plot the best route for this cluster with arrows (AntPath)
    route_coordinates = [cluster_with_destination[idx] for idx in best_route] + [chitkara_university[0]]
    antpath = AntPath(locations=route_coordinates, color=cluster_colors[i], weight=5).add_to(m)
    
    # Add markers for the stops
    for j, idx in enumerate(best_route[:-1]):  # Exclude Chitkara Univ in the loop
        folium.Marker(route_coordinates[j], popup=f"Cluster {i+1} - Stop {j+1}", 
                      icon=folium.Icon(color=cluster_colors[i])).add_to(m)
    
    # Display route info for each cluster
    print(f"Cluster {i+1} - Total Distance: {best_distance:.2f} km, Fuel Cost: {total_fuel_cost:.2f} units")

# Save and display the map
m.save("optimized_bus_routes_with_arrows.html")
