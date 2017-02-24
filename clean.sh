find . -name \*.pyc -delete
find . -name \*~ -delete
find . -type d -name '.ropeproject' -exec rm -rf {} \;
find . -type d -name '__pycache__' -exec rm -rf {} \;
