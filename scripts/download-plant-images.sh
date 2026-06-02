#!/bin/bash
# Download free plant images from Unsplash (free to use)
DIR="d:/app/java/Plant-Cultivation/frontend/public/images/plants"

declare -A IMAGES=(
  # Monstera - tropical leaf
  ["monstera"]="https://images.unsplash.com/photo-1614594975525-e45190c55d0b?w=800&q=80"
  # Pothos - trailing vine
  ["pothos"]="https://images.unsplash.com/photo-1572688484438-313a56e6dc34?w=800&q=80"
  # Snake Plant
  ["snake-plant"]="https://images.unsplash.com/photo-1593482892540-1e8a4b8d7f34?w=800&q=80"
  # Fiddle Leaf Fig
  ["fiddle-leaf-fig"]="https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=800&q=80"
  # Peace Lily
  ["peace-lily"]="https://images.unsplash.com/photo-1593691509543-c55fb32d8de5?w=800&q=80"
  # Aloe Vera
  ["aloe-vera"]="https://images.unsplash.com/photo-1567331711402-509c12c41959?w=800&q=80"
  # Spider Plant
  ["spider-plant"]="https://images.unsplash.com/photo-1572969176026-9456f39a53c4?w=800&q=80"
  # ZZ Plant
  ["zz-plant"]="https://images.unsplash.com/photo-1632207691143-643e2a9a9361?w=800&q=80"
  # Rubber Plant
  ["rubber-plant"]="https://images.unsplash.com/photo-1509423350716-97f9360b4e09?w=800&q=80"
  # Orchid
  ["orchid"]="https://images.unsplash.com/photo-1566907225472-514a62e99e79?w=800&q=80"
  # Calathea
  ["calathea"]="https://images.unsplash.com/photo-1637967886160-fd78dc3ce3f5?w=800&q=80"
  # Bird of Paradise
  ["bird-of-paradise"]="https://images.unsplash.com/photo-1600411833196-7c1f6b1a8b90?w=800&q=80"
  # Cactus
  ["cactus"]="https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=800&q=80"
  # Lavender
  ["lavender"]="https://images.unsplash.com/photo-1499002238440-d264edd596ec?w=800&q=80"
  # Basil
  ["basil"]="https://images.unsplash.com/photo-1618375569909-3c8616cf7733?w=800&q=80"
  # Jade Plant
  ["jade-plant"]="https://images.unsplash.com/photo-1509423350716-97f9360b4e09?w=800&q=80"
  # String of Pearls
  ["string-of-pearls"]="https://images.unsplash.com/photo-1597055181300-e3633a917e3e?w=800&q=80"
  # Boston Fern
  ["boston-fern"]="https://images.unsplash.com/photo-1557844352-761f2565b1e5?w=800&q=80"
  # Areca Palm
  ["areca-palm"]="https://images.unsplash.com/photo-1598880940080-ff9a29891b85?w=800&q=80"
  # Selloum
  ["selloum"]="https://images.unsplash.com/photo-1614594975525-e45190c55d0b?w=800&q=80"
  # Parlor Palm
  ["parlor-palm"]="https://images.unsplash.com/photo-1598880940080-ff9a29891b85?w=800&q=80"
  # Peperomia
  ["peperomia"]="https://images.unsplash.com/photo-1572688484438-313a56e6dc34?w=800&q=80"
  # Ponytail Palm
  ["ponytail-palm"]="https://images.unsplash.com/photo-1593482892540-1e8a4b8d7f34?w=800&q=80"
  # Philodendron
  ["philodendron"]="https://images.unsplash.com/photo-1614594975525-e45190c55d0b?w=800&q=80"
  # Chinese Money Plant
  ["chinese-money-plant"]="https://images.unsplash.com/photo-1632207691143-643e2a9a9361?w=800&q=80"
  # Chinese Evergreen
  ["chinese-evergreen"]="https://images.unsplash.com/photo-1593691509543-c55fb32d8de5?w=800&q=80"
  # Nerve Plant
  ["nerve-plant"]="https://images.unsplash.com/photo-1572969176026-9456f39a53c4?w=800&q=80"
  # Dumb Cane
  ["dumb-cane"]="https://images.unsplash.com/photo-1509423350716-97f9360b4e09?w=800&q=80"
  # China Doll
  ["china-doll"]="https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=800&q=80"
  # Arrowhead
  ["arrowhead"]="https://images.unsplash.com/photo-1572688484438-313a56e6dc34?w=800&q=80"
  # Pennywort
  ["pennywort"]="https://images.unsplash.com/photo-1557844352-761f2565b1e5?w=800&q=80"
  # Cyclamen
  ["cyclamen"]="https://images.unsplash.com/photo-1566907225472-514a62e99e79?w=800&q=80"
  # Fuchsia
  ["fuchsia"]="https://images.unsplash.com/photo-1499002238440-d264edd596ec?w=800&q=80"
  # Satin Pothos
  ["satin-pothos"]="https://images.unsplash.com/photo-1572688484438-313a56e6dc34?w=800&q=80"
  # Dragon Tree
  ["dragon-tree"]="https://images.unsplash.com/photo-1593482892540-1e8a4b8d7f34?w=800&q=80"
)

for name in "${!IMAGES[@]}"; do
  url="${IMAGES[$name]}"
  outfile="$DIR/${name}.jpg"
  if [ ! -f "$outfile" ]; then
    echo "Downloading $name..."
    curl -sL -o "$outfile" "$url" 2>/dev/null
  fi
done

echo "Done. Downloaded $(ls "$DIR"/*.jpg 2>/dev/null | wc -l) images."
