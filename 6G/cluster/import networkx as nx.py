import numpy as np
import matplotlib.pyplot as plt
from sklearn.cluster import KMeans

def generate_random_coordinates(num_points, base_lat, base_lon, spread=0.1):
    """
    Generates random latitude and longitude points around a base location.
    """
    np.random.seed(42)
    latitudes = base_lat + np.random.uniform(-spread, spread, num_points)
    longitudes = base_lon + np.random.uniform(-spread, spread, num_points)
    return np.column_stack((latitudes, longitudes))

def cluster_coordinates(locations, num_clusters):
    """
    Applies K-Means clustering to group locations into clusters.
    """
    kmeans = KMeans(n_clusters=num_clusters, n_init=10, random_state=42)
    clusters = kmeans.fit_predict(locations)
    return clusters, kmeans.cluster_centers_

def plot_routes_with_main_center(locations, clusters, cluster_centers, central_hub):
    """
    Plots the bus routes as a graph with a main center connecting all clusters.
    """
    plt.figure(figsize=(10, 8))

    num_clusters = len(set(clusters))
    colors = plt.cm.get_cmap("tab10", num_clusters)

    # Scatter plot for central hub (Main Center)
    plt.scatter(central_hub[1], central_hub[0], color="red", edgecolors="black", marker="P", s=200, label="Main Hub")

    for cluster in range(num_clusters):
        cluster_points = locations[clusters == cluster]
        center = cluster_centers[cluster]

        # Scatter plot for cluster points
        plt.scatter(cluster_points[:, 1], cluster_points[:, 0], 
                    color=colors(cluster), label=f"Cluster {cluster + 1}", alpha=0.75)

        # Draw lines connecting the cluster center to the main hub
        plt.plot([center[1], central_hub[1]], [center[0], central_hub[0]], 
                 color=colors(cluster), linestyle="-", linewidth=2, alpha=0.7)

        # Connect each stop in the cluster to its cluster center
        for point in cluster_points:
            plt.plot([point[1], center[1]], [point[0], center[0]], 
                     color=colors(cluster), linestyle="dashed", alpha=0.5)

        # Scatter plot for cluster centers
        plt.scatter(center[1], center[0], color=colors(cluster), edgecolors="black", marker="X", s=150, label=f"Cluster Center {cluster + 1}")

    plt.xlabel("Longitude")
    plt.ylabel("Latitude")
    plt.title("Bus Routes with Central Hub")
    # plt.legend()
    plt.grid(True, linestyle="--", alpha=0.5)

    # Remove background
    plt.gca().set_facecolor("white")

    # Zoom in dynamically
    plt.xlim(np.min(locations[:, 1]) - 0.01, np.max(locations[:, 1]) + 0.01)
    plt.ylim(np.min(locations[:, 0]) - 0.01, np.max(locations[:, 0]) + 0.01)

    plt.show()

def main():
    print("Generating random coordinates for bus stops...")
    
    # Example base coordinates (Chandigarh region)
    base_lat, base_lon = 30.7333, 76.7794  
    num_stops = 50  
    num_clusters = 5  

    # Generate random locations
    locations = generate_random_coordinates(num_stops, base_lat, base_lon)

    # Apply K-Means Clustering
    clusters, cluster_centers = cluster_coordinates(locations, num_clusters)

    # Define the central hub (e.g., Chitkara University)
    central_hub = np.array([30.767, 76.785])  # Fixed central point

    # Plot routes with the central hub
    plot_routes_with_main_center(locations, clusters, cluster_centers, central_hub)

if __name__ == "__main__":
    main()
