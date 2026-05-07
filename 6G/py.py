import numpy as np
import matplotlib.pyplot as plt
import matplotlib.animation as animation

# Constants
grid_size = 100
entropy_scale = 10
dt = 0.05  # time step
mass_strength = 100  # how strongly mass warps the entropy field

# Initial positions of two objects
positions = np.array([[30.0, 50.0], [70.0, 50.0]])
velocities = np.array([[0.0, 0.0], [0.0, 0.0]])

# Grid setup
x = np.linspace(0, 100, grid_size)
y = np.linspace(0, 100, grid_size)
X, Y = np.meshgrid(x, y)

def entropy_field(pos1, pos2):
    r1 = np.sqrt((X - pos1[0])**2 + (Y - pos1[1])**2) + 1e-3
    r2 = np.sqrt((X - pos2[0])**2 + (Y - pos2[1])**2) + 1e-3
    S = mass_strength / r1 + mass_strength / r2
    return S

def entropy_gradient(pos1, pos2, idx):
    other = pos2 if idx == 0 else pos1
    dx = positions[idx][0] - other[0]
    dy = positions[idx][1] - other[1]
    r = np.sqrt(dx**2 + dy**2) + 1e-6
    grad = -mass_strength * np.array([dx, dy]) / (r**3)
    return grad

fig, ax = plt.subplots()
entropy_plot = ax.imshow(entropy_field(*positions), extent=(0, 100, 0, 100), origin='lower', cmap='inferno')
dots, = ax.plot([], [], 'bo', markersize=10)

def update(frame):
    global positions, velocities

    # Update positions based on entropy gradients
    for i in range(2):
        grad = entropy_gradient(positions[0], positions[1], i)
        velocities[i] += grad * dt
        positions[i] += velocities[i] * dt

    # Update entropy field and positions
    entropy_plot.set_data(entropy_field(*positions))
    dots.set_data(positions[:, 0], positions[:, 1])
    ax.set_title(f"Frame {frame}")
    return entropy_plot, dots

ani = animation.FuncAnimation(fig, update, frames=300, interval=50, blit=False)
plt.show()
