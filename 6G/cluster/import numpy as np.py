import numpy as np
import folium
from folium import plugins

# Data points
data = np.array([[30.7476, 76.7827],
[30.7462, 76.7811],
[30.7448, 76.7795],
[30.7433, 76.7778],
[30.7419, 76.7762],
[30.7405, 76.7746],
[30.7391, 76.7730],
[30.7377, 76.7713],
[30.7363, 76.7697],
[30.7348, 76.7680],
[30.7334, 76.7663],
[30.7320, 76.7647],
[30.7306, 76.7630],
[30.7292, 76.7613],
[30.7278, 76.7596],
[30.7263, 76.7579],
[30.7249, 76.7562],
[30.7235, 76.7545],
[30.7221, 76.7528],
[30.7207, 76.7510],
[30.7193, 76.7493],
[30.7179, 76.7475],
[30.7164, 76.7458],
[30.7150, 76.7440],
[30.7136, 76.7423],
[30.7122, 76.7405],
[30.7108, 76.7387],
[30.7094, 76.7369],
[30.7080, 76.7351],
[30.7065, 76.7333],
[30.7051, 76.7315],
[30.7037, 76.7297],
[30.7023, 76.7278],
[30.7009, 76.7260],
[30.6995, 76.7242],
[30.6980, 76.7223],
[30.6966, 76.7205],
[30.6952, 76.7186],
[30.6938, 76.7168],
[30.6923, 76.7149],
[30.6909, 76.7130],
[30.6895, 76.7111],
[30.6880, 76.7092],
[30.6866, 76.7073],
[30.6852, 76.7053],
[30.6837, 76.7034],
[30.6823, 76.7014],
[30.6809, 76.6995],
[30.6794, 76.6975],
[30.6780, 76.6955],
[30.6766, 76.6935],
[30.6751, 76.6915],
[30.6737, 76.6895],
[30.6723, 76.6875],
[30.6708, 76.6855],
[30.6694, 76.6835]])


# Define the distance function
def euclidean_distance(p1, p2):
    return np.sqrt(np.sum((p1 - p2)**2))

# Function to check if two points are within 3 km of each other
def within_3_km(p1, p2):
    return euclidean_distance(p1, p2) <= 3

# Initialize clusters
clusters = []

# Iterate through each point to form clusters
for i, point in enumerate(data):
    # Find a cluster for the point
    assigned = False
    for cluster in clusters:
        if len(cluster) < 4 and all(within_3_km(point, data[j]) for j in cluster):
            cluster.append(i)
            assigned = True
            break
    if not assigned:
        # If no suitable cluster found, create a new cluster
        clusters.append([i])

# Create a map centered at the mean of all points
center_lat = np.mean(data[:, 0])
center_lon = np.mean(data[:, 1])
map_clusters = folium.Map(location=[center_lat, center_lon], zoom_start=10)

# Add markers for original points
for i, point in enumerate(data):
    folium.Marker(location=point, popup=f'Point {i}').add_to(map_clusters)

# Add lines between original points and their closest points
for cluster in clusters:
    for i in range(len(cluster)):
        for j in range(i + 1, len(cluster)):
            points = [data[cluster[i]].tolist(), data[cluster[j]].tolist()]
            folium.PolyLine(points, color='red', weight=2.5, opacity=1).add_to(map_clusters)

# Display the map
map_clusters.save('clusters_map.html')
