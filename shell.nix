{ pkgs ? import (builtins.fetchTarball "https://github.com/NixOS/nixpkgs/archive/8bb5646e0bed5dbd3ab08c7a7cc15b75ab4e1d0f.tar.gz") {} }:
let
	libs = with pkgs; [
		glfw3-minecraft
		libGL
		xorg.libX11
		xorg.libXcursor
		xorg.libXext
		xorg.libXrandr
		xorg.libXxf86vm
		vulkan-loader
		flite
	];
in
pkgs.mkShell {
	nativeBuildInputs = with pkgs.buildPackages; [ gradle_8 jdk17 glfw glfw3-minecraft ];
	shellHook = "exec zsh";
	buildInputs = libs;
	LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath libs;
}
