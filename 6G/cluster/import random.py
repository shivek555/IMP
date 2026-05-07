import random
import numpy as np
import folium
import math
from sklearn.cluster import KMeans
from ortools.constraint_solver import routing_enums_pb2
from ortools.constraint_solver import pywrapcp

# 1. Data Setup: Generate coordinates for Ambala Cantt, Chandigarh, Patiala, and Chitkara University
areas = {
    "Ambala Cantt": (30.3782, 76.7767),
    "Chandigarh": (30.7333, 76.7794),
    "Patiala": (30.3398, 76.3869),
    "Chitkara University": (30.516363782063365, 76.65999237672537)  # Exact coordinates for Chitkara University
}

# Generate 50 random coordinates around these cities
np.random.seed(42)
coordinates = []

for _ in range(50):
    lat = random.uniform(30.2, 30.8)
    lon = random.uniform(76.3, 76.9)
    coordinates.append((lat, lon))

# Combine fixed locations and generated coordinates
all_locations = list(areas.values()) + coordinates

# 2. Clustering: Use KMeans to cluster the coordinates into small clusters of max 3 points per cluster
num_clusters = len(coordinates) // 3  # Ensure that each cluster has max 3 points
kmeans = KMeans(n_clusters=num_clusters, random_state=42, n_init='auto')
kmeans.fit(np.array(coordinates))
cluster_labels = kmeans.labels_

# 3. Haversine formula for distance calculation
def haversine(coord1, coord2):
    R = 6371  # Earth radius in kilometers
    lat1, lon1 = coord1
    lat2, lon2 = coord2
    
    dlat = math.radians(lat2 - lat1)
    dlon = math.radians(lon2 - lon1)
    a = math.sin(dlat / 2) ** 2 + math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) * math.sin(dlon / 2) ** 2
    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))
    return R * c

# Update the distance_matrix function to use Haversine distance
def distance_matrix(locations):
    size = len(locations)
    dist_matrix = np.zeros((size, size))
    for i in range(size):
        for j in range(size):
            dist_matrix[i][j] = haversine(locations[i], locations[j])
    return dist_matrix

# Distance matrix for all locations
full_distance_matrix = distance_matrix(all_locations)

def create_data_model(cluster):
    data = {}
    data['distance_matrix'] = distance_matrix(cluster)
    data['num_vehicles'] = 1  # 1 vehicle for each cluster
    data['depot'] = len(cluster) - 1  # Chitkara University as depot (last in cluster)
    return data

def optimize_route(data):
    # Manager expects the correct number of locations (i.e., just the cluster)
    manager = pywrapcp.RoutingIndexManager(len(data['distance_matrix']), data['num_vehicles'], data['depot'])
    routing = pywrapcp.RoutingModel(manager)

    def distance_callback(from_index, to_index):
        from_node = manager.IndexToNode(from_index)
        to_node = manager.IndexToNode(to_index)
        return int(data['distance_matrix'][from_node][to_node] * 1000)  # Multiply by 1000 to convert to meters

    transit_callback_index = routing.RegisterTransitCallback(distance_callback)
    routing.SetArcCostEvaluatorOfAllVehicles(transit_callback_index)

    search_parameters = pywrapcp.DefaultRoutingSearchParameters()
    search_parameters.first_solution_strategy = (routing_enums_pb2.FirstSolutionStrategy.PATH_CHEAPEST_ARC)
    
    solution = routing.SolveWithParameters(search_parameters)
    if solution:
        return solution, routing, manager
    else:
        print("No solution found!")
        return None, None, None

# 4. Create Folium Map and Run Route Optimization for each Cluster
m = folium.Map(location=areas["Chitkara University"], zoom_start=10)
colors = ['red', 'blue', 'green', 'purple', 'orange']

total_distance = 0
total_cost = 0
route_descriptions = []

for i in range(num_clusters):
    # Get the coordinates of the bus stops in the current cluster
    cluster_coords = [coordinates[j] for j in range(len(coordinates)) if cluster_labels[j] == i]
    
    # Add each stop to the map
    for coord in cluster_coords:
        folium.Marker(location=coord, popup=f'Stop in Cluster {i+1}', icon=folium.Icon(color=colors[i % len(colors)])).add_to(m)
    
    # Add Chitkara University to the cluster
    cluster_coords.append(areas["Chitkara University"])
    
    # Prepare data and optimize route for the current cluster
    data = create_data_model(cluster_coords)
    solution, routing, manager = optimize_route(data)
    
    if solution:
        # Extract the best route and calculate the total distance
        route = []
        index = routing.Start(0)
        route_distance = 0
        
        while not routing.IsEnd(index):
            node_index = manager.IndexToNode(index)
            route.append(node_index)
            previous_index = index
            index = solution.Value(routing.NextVar(index))
            route_distance += routing.GetArcCostForVehicle(previous_index, index, 0)

        # Convert distance back to kilometers
        route_distance /= 1000.0
        
        # Add the route distance to total distance
        total_distance += route_distance
        
        # Add Chitkara University (last stop) and create a textual representation of the route
        route_description = " -> ".join([chr(65 + idx) for idx in route])
        route_descriptions.append(f"Cluster {i+1}: {route_description}, Distance: {route_distance:.2f} km")
        
        # Visualize the route on the map
        route_coords = [cluster_coords[j] for j in route]
        folium.PolyLine(locations=route_coords, color=colors[i % len(colors)], weight=2.5, opacity=1).add_to(m)

        # Calculate cost for the cluster and add a marker
        cluster_cost = route_distance * 10  # Assuming 10 units per km
        total_cost += cluster_cost
        folium.Marker(location=cluster_coords[-1], popup=f"Cluster {i+1} Cost: {cluster_cost:.2f} units", icon=folium.Icon(color=colors[i % len(colors)])).add_to(m)

# Add a marker to show the total distance and cost
folium.Marker(location=areas["Chitkara University"], popup=f'Total Distance: {total_distance:.2f} km<br>Total Cost: {total_cost:.2f} units', icon=folium.Icon(color='black')).add_to(m)

# Save the map as an HTML file
m.save('optimized_bus_routes.html')

# Output the total cost and best routes
print(f"Total Cost: {total_cost:.2f} units")
for route_description in route_descriptions:
    print(route_description)
