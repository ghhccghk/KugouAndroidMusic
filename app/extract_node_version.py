import re

with open("app/src/main/cpp/node/node_version.h", encoding="utf-8") as f:
    content = f.read()

major = re.search(r"#define NODE_MAJOR_VERSION (\d+)", content).group(1)
minor = re.search(r"#define NODE_MINOR_VERSION (\d+)", content).group(1)
patch = re.search(r"#define NODE_PATCH_VERSION (\d+)", content).group(1)

version = f"v{major}.{minor}.{patch}"
print(version, end="")
