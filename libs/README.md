# JACOB Native Libraries

This directory contains the native DLL files required for JACOB (Java-COM Bridge) to work.

## Files

- `jacob-x64.dll` - 64-bit Windows DLL for JACOB
- `jacob-x86.dll` - 32-bit Windows DLL for JACOB

## Usage

These DLLs are automatically copied to the classpath during the Maven build process. The DLL that matches your JVM architecture (32-bit or 64-bit) will be loaded at runtime.

## Requirements

- Windows operating system (JACOB uses Windows COM/ActiveX)
- Java JVM that matches the DLL architecture (use 64-bit JVM for jacob-x64.dll)
- The DLLs must be either:
  - In the classpath (handled automatically by the build)
  - In the `java.library.path`
  - In the Windows System32 directory
  - In the current working directory

## ZKTeco Device Connection

This application uses JACOB to connect to ZKTeco biometric devices (like the iFace 702) via the zkemkeeper COM component. Make sure you have:

1. ZKTeco SDK/Driver installed on the Windows machine
2. The zkemkeeper.dll registered on your system
3. Proper network connectivity to the device

## Troubleshooting

If you get `UnsatisfiedLinkError`:
- Verify that the JVM architecture matches the DLL (64-bit JVM needs jacob-x64.dll)
- Check that the DLL is in the classpath or java.library.path
- Ensure the ZKTeco SDK is properly installed and registered

If you get `Can't co-create object`:
- Verify the ZKTeco SDK is installed
- Check that zkemkeeper.dll is registered using `regsvr32 zkemkeeper.dll`
- Run the application with administrator privileges if needed
