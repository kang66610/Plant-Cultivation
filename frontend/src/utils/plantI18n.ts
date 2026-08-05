import type { Plant } from '@/types'

type Translate = (key: string) => string

export const plantCategoryOptions = [
  { slug: 'succulents', emoji: '🌵' },
  { slug: 'tropical', emoji: '🌿' },
  { slug: 'herbs', emoji: '🌱' },
  { slug: 'ferns', emoji: '🌾' },
  { slug: 'flowering', emoji: '🌸' },
  { slug: 'trees', emoji: '🌳' },
]

const plantCopy: Record<string, { name: string; description: string }> = {
  'monstera-deliciosa': {
    name: 'Monstera',
    description: 'Iconic split-leaf tropical plant that brings a lush jungle feeling indoors.',
  },
  pothos: {
    name: 'Pothos',
    description: 'Easy trailing vine for beginners, shelves, and hanging baskets.',
  },
  'snake-plant': {
    name: 'Snake plant',
    description: 'Nearly indestructible upright plant that purifies air day and night.',
  },
  'fiddle-leaf-fig': {
    name: 'Fiddle leaf fig',
    description: 'Dramatic statement plant with large violin-shaped leaves.',
  },
  'peace-lily': {
    name: 'Peace lily',
    description: 'Elegant flowering plant that clearly signals when it needs water.',
  },
  'aloe-vera': {
    name: 'Aloe vera',
    description: 'Medicinal succulent with soothing gel, perfect for a sunny windowsill.',
  },
  'spider-plant': {
    name: 'Spider plant',
    description: 'Graceful arching leaves with baby plantlets, very forgiving and easy to grow.',
  },
  'zz-plant': {
    name: 'ZZ plant',
    description: 'Glossy architectural plant that thrives even with low attention.',
  },
  'rubber-plant': {
    name: 'Rubber plant',
    description: 'Bold indoor tree with glossy leaves and strong visual presence.',
  },
  'boston-fern': {
    name: 'Boston fern',
    description: 'Classic fern with lush arching fronds that softens bright indoor corners.',
  },
  'jade-plant': {
    name: 'Jade plant',
    description: 'Woody succulent with plump leaves that stores water and grows slowly.',
  },
  basil: {
    name: 'Basil',
    description: 'Fast-growing culinary herb with fragrant leaves that prefers bright light and steady moisture.',
  },
  'ocimum-basilicum': {
    name: 'Basil',
    description: 'Fast-growing culinary herb with fragrant leaves that prefers bright light and steady moisture.',
  },
  lavender: {
    name: 'Lavender',
    description: 'Sun-loving aromatic herb with purple blooms and a calming fragrance.',
  },
  'lavandula-angustifolia': {
    name: 'Lavender',
    description: 'Sun-loving aromatic herb with purple blooms and a calming fragrance.',
  },
  orchid: {
    name: 'Orchid',
    description: 'Elegant blooming orchid with flowers that can last for months.',
  },
  calathea: {
    name: 'Calathea',
    description: 'Patterned prayer plant known for striking leaf designs.',
  },
  'bird-of-paradise': {
    name: 'Bird of paradise',
    description: 'Dramatic tropical focal plant with broad banana-like leaves.',
  },
  'string-of-pearls': {
    name: 'String of pearls',
    description: 'Trailing succulent with bead-like leaves for shelves and hanging pots.',
  },
  'chinese-money-plant': {
    name: 'Chinese money plant',
    description: 'Playful round-leaf plant with coin-shaped foliage and a compact habit.',
  },
  pennywort: {
    name: 'Pennywort',
    description: 'Round-leaf moisture-loving plant that grows happily in soil or water.',
  },
  'hydrocotyle-vulgaris': {
    name: 'Pennywort',
    description: 'Round-leaf moisture-loving plant that grows happily in soil or water.',
  },
  philodendron: {
    name: 'Heartleaf philodendron',
    description: 'Adaptable trailing philodendron with soft heart-shaped leaves.',
  },
  'heartleaf-philodendron': {
    name: 'Heartleaf philodendron',
    description: 'Adaptable trailing philodendron with soft heart-shaped leaves.',
  },
  'philodendron-hederaceum': {
    name: 'Heartleaf philodendron',
    description: 'Adaptable trailing philodendron with soft heart-shaped leaves.',
  },
  cactus: {
    name: 'Cactus',
    description: 'Desert survivor with sculptural stems and excellent drought tolerance.',
  },
  'areca-palm': {
    name: 'Areca palm',
    description: 'Air-purifying palm with soft, graceful fronds.',
  },
  selloum: {
    name: 'Selloum',
    description: 'Large tropical foliage plant with deeply cut decorative leaves.',
  },
  'china-doll': {
    name: 'China doll',
    description: 'Fine-leaved indoor tree with a fresh, airy canopy.',
  },
  'nerve-plant': {
    name: 'Nerve plant',
    description: 'Small humidity-loving plant with vivid vein patterns on soft leaves.',
  },
  'parlor-palm': {
    name: 'Parlor palm',
    description: 'Compact, elegant palm for desks, shelves, and low-light corners.',
  },
  peperomia: {
    name: 'Peperomia',
    description: 'Small, glossy-leaved plant that is simple to care for.',
  },
  'ponytail-palm': {
    name: 'Ponytail palm',
    description: 'Distinctive bottle-shaped trunk stores water and tolerates neglect.',
  },
  'dumb-cane': {
    name: 'Dumb cane',
    description: 'Bold patterned foliage plant that brings strong tropical texture indoors.',
  },
  arrowhead: {
    name: 'Arrowhead plant',
    description: 'Humidity-loving vine with arrow-shaped leaves and easy indoor growth.',
  },
  'syngonium-podophyllum': {
    name: 'Arrowhead plant',
    description: 'Humidity-loving vine with arrow-shaped leaves and easy indoor growth.',
  },
  'satin-pothos': {
    name: 'Satin pothos',
    description: 'Elegant trailing vine with silver-green speckled leaves.',
  },
  'chinese-evergreen': {
    name: 'Chinese evergreen',
    description: 'Shade-tolerant foliage plant with refined silver-white markings.',
  },
  'dragon-tree': {
    name: 'Dragon tree',
    description: 'Slim architectural indoor tree with a clean modern silhouette.',
  },
  cyclamen: {
    name: 'Cyclamen',
    description: 'Elegant winter-blooming plant with distinctive, delicate flowers.',
  },
  fuchsia: {
    name: 'Fuchsia',
    description: 'Hanging lantern-shaped flowers with vivid color and movement.',
  },
  'pieris-japonica': {
    name: 'Pieris',
    description: 'Graceful Japanese-style ornamental shrub with refined bonsai character.',
  },
  'tradescantia-quadricolor': {
    name: 'Tradescantia quadricolor',
    description: 'Dreamy trailing plant with pink, white, green, and purple foliage.',
  },
  'philodendron-panduriforme': {
    name: 'Panduriforme philodendron',
    description: 'Tropical statement plant with broad fiddle-shaped leaves.',
  },
  'pilea-cadierei': {
    name: 'Aluminum plant',
    description: 'Shade-tolerant small plant with metallic silver-green leaf markings.',
  },
  'asplenium-nidus': {
    name: 'Bird nest fern',
    description: 'Bright green fern with smooth fronds arranged in a nest-like rosette.',
  },
  'alocasia-amazonica': {
    name: 'Alocasia Amazonica',
    description: 'Dark arrow-shaped leaves with sharp silver veins and a modern look.',
  },
  'philodendron-imperial-green': {
    name: 'Imperial green philodendron',
    description: 'Robust glossy philodendron with strong, upright leaves.',
  },
  'alocasia-cucullata': {
    name: 'Buddha hand alocasia',
    description: 'Heart-shaped leaves that grow in dense, layered clusters.',
  },
  'caladium-bicolor': {
    name: 'Caladium',
    description: 'Thin, colorful leaves with painterly white, pink, and green patterns.',
  },
  'hedera-helix': {
    name: 'English ivy',
    description: 'Trailing ivy with shaped leaves, hardy growth, and air-cleaning ability.',
  },
  'murraya-exotica': {
    name: 'Orange jasmine',
    description: 'Evergreen bonsai-style shrub with white flowers and a sweet fragrance.',
  },
  'helianthus-annuus': {
    name: 'Sunflower',
    description: 'Bright golden flower that follows the sun and brings lively energy.',
  },
  'mentha-canadensis': {
    name: 'Mint',
    description: 'Fresh aromatic herb that grows quickly and is useful in the kitchen.',
  },
  'hydrangea-macrophylla-endless-summer': {
    name: 'Endless summer hydrangea',
    description: 'Color-changing hydrangea that blooms repeatedly on old and new stems.',
  },
  'echeveria-chihuahuensis': {
    name: 'Echeveria',
    description: 'Compact rosette succulent with frosted leaves and red-tipped edges.',
  },
  'oncidium-flexuosum': {
    name: 'Oncidium orchid',
    description: 'Dancing yellow orchid with a sweet, chocolate-like fragrance.',
  },
}

const scientificNameCopy: Record<string, { name: string; description?: string }> = {
  'Monstera deliciosa': plantCopy['monstera-deliciosa'],
  'Epipremnum aureum': plantCopy.pothos,
  'Dracaena trifasciata': plantCopy['snake-plant'],
  'Ficus lyrata': plantCopy['fiddle-leaf-fig'],
  'Spathiphyllum wallisii': plantCopy['peace-lily'],
  'Aloe barbadensis miller': plantCopy['aloe-vera'],
  'Chlorophytum comosum': plantCopy['spider-plant'],
  'Zamioculcas zamiifolia': plantCopy['zz-plant'],
  'Ficus elastica': plantCopy['rubber-plant'],
  'Nephrolepis exaltata': plantCopy['boston-fern'],
  'Crassula ovata': plantCopy['jade-plant'],
  'Lavandula angustifolia': plantCopy.lavender,
  'Ocimum basilicum': plantCopy.basil,
  'Phalaenopsis spp.': plantCopy.orchid,
  'Calathea spp.': plantCopy.calathea,
  'Strelitzia reginae': plantCopy['bird-of-paradise'],
  'Curio rowleyanus': plantCopy['string-of-pearls'],
  'Philodendron hederaceum': plantCopy['philodendron-hederaceum'],
  'Pilea peperomioides': plantCopy['chinese-money-plant'],
  'Cactaceae spp.': plantCopy.cactus,
  'Dypsis lutescens': plantCopy['areca-palm'],
  'Thaumatophyllum bipinnatifidum': plantCopy.selloum,
  'Hydrocotyle vulgaris': plantCopy.pennywort,
  'Radermachera sinica': plantCopy['china-doll'],
  'Fittonia spp.': plantCopy['nerve-plant'],
  'Chamaedorea elegans': plantCopy['parlor-palm'],
  'Peperomia obtusifolia': plantCopy.peperomia,
  'Dieffenbachia spp.': plantCopy['dumb-cane'],
  'Dieffenbachia seguine': plantCopy['dumb-cane'],
  'Beaucarnea recurvata': plantCopy['ponytail-palm'],
  'Syngonium podophyllum': plantCopy['syngonium-podophyllum'],
  'Scindapsus pictus': plantCopy['satin-pothos'],
  'Aglaonema commutatum': plantCopy['chinese-evergreen'],
  'Dracaena marginata': plantCopy['dragon-tree'],
  'Cyclamen persicum': plantCopy.cyclamen,
  'Fuchsia spp.': plantCopy.fuchsia,
  'Pieris japonica': plantCopy['pieris-japonica'],
  'Tradescantia fluminensis Quadricolor': plantCopy['tradescantia-quadricolor'],
  'Philodendron panduriforme': plantCopy['philodendron-panduriforme'],
  'Pilea cadierei': plantCopy['pilea-cadierei'],
  'Asplenium nidus': plantCopy['asplenium-nidus'],
  'Alocasia amazonica': plantCopy['alocasia-amazonica'],
  'Philodendron Imperial Green': plantCopy['philodendron-imperial-green'],
  'Alocasia cucullata': plantCopy['alocasia-cucullata'],
  'Caladium bicolor': plantCopy['caladium-bicolor'],
  'Hedera helix': plantCopy['hedera-helix'],
  'Murraya exotica': plantCopy['murraya-exotica'],
  'Helianthus annuus': plantCopy['helianthus-annuus'],
  'Mentha canadensis': plantCopy['mentha-canadensis'],
  'Hydrangea macrophylla Endless Summer': plantCopy['hydrangea-macrophylla-endless-summer'],
  'Echeveria chihuahuensis': plantCopy['echeveria-chihuahuensis'],
  'Oncidium flexuosum': plantCopy['oncidium-flexuosum'],
}

function translatedValue(key: string, fallback: string, t: Translate) {
  const value = t(key)
  return value === key ? fallback : value
}

export function getCategoryDisplayName(slug: string, fallback: string, t: Translate) {
  return translatedValue(`categories.${slug}`, fallback || slug, t)
}

export function getPlantDisplayName(
  plant: Pick<Plant, 'slug' | 'commonName'> & Partial<Pick<Plant, 'scientificName'>>,
  locale: string
) {
  if (locale === 'zh-CN') return plant.commonName
  return plantCopy[plant.slug]?.name || scientificNameCopy[plant.scientificName || '']?.name || plant.commonName
}

export function getPlantDisplayDescription(
  plant: Pick<Plant, 'slug' | 'shortDescription'> & Partial<Pick<Plant, 'scientificName'>>,
  locale: string
) {
  if (locale === 'zh-CN') return plant.shortDescription
  return plantCopy[plant.slug]?.description || scientificNameCopy[plant.scientificName || '']?.description || plant.shortDescription
}

export function getPlantDisplayNameByScientificName(scientificName: string, fallback: string, locale: string) {
  return locale === 'zh-CN' ? fallback : scientificNameCopy[scientificName]?.name || fallback
}
