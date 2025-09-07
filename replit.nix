{ pkgs }: {
  deps = [
    # Java Development Kit
    pkgs.openjdk17
    
    # Maven for building Java project
    pkgs.maven
    
    # Python for AI agents and SAM
    pkgs.python311
    pkgs.python311Packages.pip
    pkgs.python311Packages.setuptools
    pkgs.python311Packages.wheel
    pkgs.python311Packages.virtualenv
    
    # Docker for Solace event broker
    pkgs.docker
    pkgs.docker-compose
    
    # PostgreSQL for database
    pkgs.postgresql
    
    # Node.js for Vaadin frontend compilation
    pkgs.nodejs-18_x
    pkgs.nodePackages.npm
    
    # Git for version control
    pkgs.git
    
    # Process management
    pkgs.procps
    pkgs.killall
    pkgs.ps
    
    # Network utilities
    pkgs.curl
    pkgs.wget
    pkgs.netcat
    pkgs.lsof
    
    # Text processing
    pkgs.jq
    
    # Development tools
    pkgs.which
    pkgs.findutils
    pkgs.gnused
    pkgs.gawk
    pkgs.bash
    pkgs.coreutils
    
    # SSL/TLS support
    pkgs.openssl
    pkgs.cacert
  ];
  
  env = {
    # Java Environment
    JAVA_HOME = "${pkgs.openjdk17}/lib/openjdk";
    PATH = "${pkgs.openjdk17}/bin:${pkgs.maven}/bin:${pkgs.python311}/bin:${pkgs.nodejs-18_x}/bin:${pkgs.postgresql}/bin:$PATH";
    
    # Maven configuration
    MAVEN_OPTS = "-Xmx2048m -Xms512m";
    M2_HOME = "${pkgs.maven}";
    
    # Python environment
    PYTHONPATH = "$PYTHONPATH:${toString ./.}/agents";
    PYTHON_PATH = "${pkgs.python311}/bin/python";
    
    # PostgreSQL configuration
    PGDATA = "$REPL_HOME/postgres";
    PGUSER = "main";
    PGDATABASE = "main";
    
    # Node.js configuration  
    NODE_ENV = "development";
    
    # Vaadin configuration for Replit
    VAADIN_OFFLINE_KEY = "";
    
    # SSL Configuration
    SSL_CERT_FILE = "${pkgs.cacert}/etc/ssl/certs/ca-bundle.crt";
    SSL_CERT_DIR = "${pkgs.cacert}/etc/ssl/certs";
  };
}
