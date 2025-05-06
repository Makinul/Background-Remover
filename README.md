# Background Remover

A user-friendly Android application for removing backgrounds from images.

## Overview

Background Remover is an Android application designed to efficiently and effectively remove the background from images. Using image segmentation, this application allows you to isolate the main subject of a photo and discard the surrounding background, providing a transparent image result.

## Features

*   **Background Removal:** Quickly and easily remove backgrounds from images.
*   **Image Segmentation:** Uses efficient image segmentation algorithms to identify and isolate the subject from its background.
*   **User-Friendly Interface:** Simple and intuitive design for ease of use.
*   **Transparent Output:** Exports images with a transparent background.

## Getting Started

### Prerequisites

*   Android Studio (latest version recommended)
*   Android SDK (API level 21 or higher)

### Installation

1.  Clone the repository to your local machine:Replace `[repository-url]` with the actual URL of your repository.
2.  Open the project in Android Studio.
3.  Wait for Gradle to sync and build the project.

### Usage

1.  Launch the application on an emulator or a physical device.
2.  Select an image from your device's gallery or take a new one using the camera.
3.  Use the app's interface to trigger the background removal process.
4.  The app will process the image and display the result with the background removed.
5.  Save the new image in your local device.

## Architecture

- Modern Android Development Architecture.
- Use Hilt dependency injection

## Contributing

Contributions are welcome! If you want to contribute to the project, please follow these steps:

1.  Fork the repository.
2.  Create a new branch (`git checkout -b feature/new-feature`).
3.  Make your changes.
4.  Commit your changes (`git commit -am 'Add some feature'`).
5.  Push to the branch (`git push origin feature/new-feature`).
6.  Create a new Pull Request.

## License

MIT License

## Contact

Md. Makinul Hasan Khan Nasim - md.makinul.nasim@gmail.com

## Notes

* The splash screen is implemented using `SplashActivity`
* There are two activities: `SplashActivity` and `HomeActivity`.
*  The project is structured to use Hilt for dependency injection.
*  The project uses the modern `androidx` libraries.
