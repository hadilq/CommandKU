let pkgs = import <nixpkgs> {};
in pkgs.mkShell {
  buildInputs = with pkgs; [
    openjdk
  ];
  shellHook = ''
    export JAVA_HOME=${pkgs.openjdk}
    '';
}
