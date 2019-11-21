from setuptools import find_packages, setup

setup(
    name="Busy Engineer's Document Bucket",
    version="0.1.0",
    packages=find_packages("src"),
    package_dir={"": "src"},
    data_files=[("config", ["config/config.toml"])],
)
