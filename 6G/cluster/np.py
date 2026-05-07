import numpy as np
import matplotlib.pyplot as plt
from sklearn.cluster import KMeans
import networkx as nx
from scipy.spatial import distance_matrix
import tkinter as tk
from tkinter.scrolledtext import ScrolledText

# Seed for reproducibility
np.random.seed(42)

# Constants
num_clusters = 5
points_per_cluster = 8
main_hub = np.array([[76.775, 30.760]])  # Central hub coordinates

# Define cluster centers
cluster_centers = np.array([
    [76.700, 30.725],
    [76.750, 30.800],
    [76.825, 30.825],
    [76.700, 30.650],
    [76.825, 30.700]
])

# Generate points around each cluster center
points = []
for center in cluster_centers:
    points.append(center + (np.random.rand(points_per_cluster, 2) - 0.5) * 0.05)
points = np.vstack(points)

# KMeans clustering
kmeans = KMeans(n_clusters=num_clusters, random_state=42, n_init=10)
clusters = kmeans.fit_predict(points)
sub_centers = kmeans.cluster_centers_

# Setup
colors = ['blue', 'green', 'brown', 'gray', 'cyan']
cluster_labels = ["A", "B", "C", "D", "E"]
legend_labels = {}

# ---- Plotting ----
plt.figure(figsize=(16, 10))

for i in range(num_clusters):
    cluster_points = points[clusters == i]
    sub_center = sub_centers[i]

    # Build MST using distance matrix
    dist_mat = distance_matrix(cluster_points, cluster_points)
    G = nx.Graph()
    for j in range(len(cluster_points)):
        for k in range(j + 1, len(cluster_points)):
            G.add_edge(j, k, weight=dist_mat[j, k])
    mst = nx.minimum_spanning_tree(G)

    # Get MST traversal order
    mst_order = list(nx.dfs_preorder_nodes(mst, source=0))
    ordered_labels = [f"{cluster_labels[i]}{j + 1}" for j in mst_order]
    legend_labels[f"Cluster {cluster_labels[i]}"] = " → ".join(ordered_labels)

    # Draw MST edges
    for edge in mst.edges:
        p1, p2 = cluster_points[edge[0]], cluster_points[edge[1]]
        plt.plot([p1[0], p2[0]], [p1[1], p2[1]], linestyle='--', color=colors[i], alpha=0.7)

    # Draw lines from sub-center to each point
    for j, p in enumerate(cluster_points):
        plt.plot([sub_center[0], p[0]], [sub_center[1], p[1]], linestyle='--', color=colors[i], alpha=0.4)
        plt.text(p[0], p[1], f"{cluster_labels[i]}{j+1}", fontsize=10, color=colors[i])

    # Draw and label sub-center
    plt.scatter(sub_center[0], sub_center[1], marker='X', s=250, color=colors[i], edgecolor='black')
    plt.text(sub_center[0], sub_center[1], f"Cluster {cluster_labels[i]}", fontsize=12, weight='bold', color=colors[i])

    # Connect sub-center to main hub
    plt.plot([main_hub[0, 0], sub_center[0]], [main_hub[0, 1], sub_center[1]],
             linestyle='-', color=colors[i], linewidth=2.5)

# Plot main hub
plt.scatter(main_hub[:, 0], main_hub[:, 1], color='red', marker='P', s=350, edgecolor='black')
plt.text(main_hub[0, 0], main_hub[0, 1], "Main Hub", fontsize=14, weight='bold', color='red')

# Final graph settings
plt.title("Optimized Bus Routes with MST and Central Hub", fontsize=16)
plt.xlabel("Longitude", fontsize=12)
plt.ylabel("Latitude", fontsize=12)
plt.grid(True)
plt.tight_layout()
plt.show()

# ---- Tkinter Legend Tab ----
def show_legend_tab():
    window = tk.Tk()
    window.title("Cluster Route Sequences")
    window.geometry("750x450")

    text_box = ScrolledText(window, font=("Consolas", 12), wrap=tk.WORD)
    text_box.pack(expand=True, fill='both')

    for cluster, path in legend_labels.items():
        text_box.insert(tk.END, f"{cluster}:\n{path}\n\n")

    window.mainloop()

show_legend_tab()
