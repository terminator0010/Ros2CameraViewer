import pybullet as p
import time
import os

os.environ["PYTHONUTF8"] = "1"
os.system("xacro Cessna172p.xacro -o cessna_172.urdf")

p.connect(p.GUI)

try:
    aviao = p.loadURDF("cessna_172.urdf", useFixedBase=True)
except Exception as e:
    print(f"\nErro ao carregar o URDF! Detalhes: {e}")
    print("Verifique se as pastas de 'meshes' (malhas 3D) estão no local correto.")

while p.isConnected():
    time.sleep(1./240.)
