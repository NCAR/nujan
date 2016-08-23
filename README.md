# Nujan
This is the official home for Nujan source code.  Nujan was developed by the NCAR Research Applications Laboratory as a pure Java netCDF-4 and HDF-5 writer.  

Nujan is 100% open source and is released under the MIT license. Nujan is intended to be useful in situations where portability and a simplified development process are more important than access to the complete HDF5 feature set.  Nujan creates files compatible with:

* HDF5 1.8.5 and later
* NetCDF 4.1.1 and later

The primary differences between Nujan and the existing JNI-based HDF5 Java writer in the Java netCDF API are:

* Nujan does not depend on any C code, so does not use the JNI (Java Native Interface)
* The Nujan writer only writes new files. It does not update existing files.
* Nujan supports most of the commonly used features of NetCDF4 and HDF5, but does not implement all features
