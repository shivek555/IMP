import numpy as np
import matplotlib.pyplot as plt

# New set of data points with floating-point coordinates
new_data = np.array([[30.7476, 76.7827],
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

# Function to check if a point is within 3 km of the center point
def within_3_km(p1, p2):
    return euclidean_distance(p1, p2) <= 3.5

# Initialize clusters
clusters = []

# Iterate through each data point to form clusters
for point in new_data:
    # Flag to check if the point is added to any existing cluster
    added_to_cluster = False
    
    # Check if the point is within 3 km of any existing cluster center
    for cluster_center, cluster_points in clusters:
        if within_3_km(point, cluster_center):
            cluster_points.append(point)
            added_to_cluster = True
            break
    
    # If the point is not within 3 km of any existing cluster center, create a new cluster
    if not added_to_cluster:
        clusters.append((point, [point]))  # Initialize cluster with the center point itself

# Plot the clusters with circles
plt.figure(figsize=(8, 8))
for i, (cluster_center, cluster_points) in enumerate(clusters):
    cluster_points = np.array(cluster_points)
    plt.scatter(cluster_points[:, 0], cluster_points[:, 1], label=f'Cluster {i+1} Center: {cluster_center}')
    
    # Plot circle around cluster center
    circle = plt.Circle(cluster_center, radius=3, color=plt.cm.tab10(i), fill=False)
    plt.gca().add_patch(circle)

plt.title('Clusters of Data Points with Circles')
plt.xlabel('X')
plt.ylabel('Y')
plt.legend()
plt.grid(True)
plt.axis('equal')  # Equal aspect ratio for x and y axes
plt.show()
