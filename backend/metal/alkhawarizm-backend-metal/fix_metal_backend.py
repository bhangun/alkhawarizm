import re

with open("src/main/java/tech/kayys/alkhawarizm/backend/metal/MetalComputeBackend.java", "r") as f:
    code = f.read()

code = code.replace("););", "));")

with open("src/main/java/tech/kayys/alkhawarizm/backend/metal/MetalComputeBackend.java", "w") as f:
    f.write(code)
