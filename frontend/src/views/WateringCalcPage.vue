<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

// ─── Types ──────────────────────────────────────────────────
interface CalcPlant {
  id: number
  commonName: string
  scientificName: string
  emoji: string
  baseFrequencyDays: [number, number]   // min-max range
  tips: string[]
  lowLightAdj: number
  highLightAdj: number
  brightDirectAdj: number
  outdoorAdj: number
  summerAdj: number
  winterAdj: number
}

type PotSize = 'small' | 'medium' | 'large' | 'xlarge'
type Environment = 'indoor' | 'outdoor'
type LightLevel = 'low' | 'medium' | 'high' | 'bright_direct'
type Season = 'spring' | 'summer' | 'fall' | 'winter'

// ─── Mock data ──────────────────────────────────────────────
const mockPlants: CalcPlant[] = [
  {
    id: 1, commonName: '龟背竹', scientificName: 'Monstera deliciosa', emoji: '🌿',
    baseFrequencyDays: [7, 10], lowLightAdj: 3, highLightAdj: -1, brightDirectAdj: -2, outdoorAdj: -2, summerAdj: -2, winterAdj: 4,
    tips: ['允许土壤顶部2-3英寸在浇水之间干燥', '定期给叶片喷雾以增加湿度', '冬季减少浇水'],
  },
  {
    id: 2, commonName: '绿萝', scientificName: 'Epipremnum aureum', emoji: '🌿',
    baseFrequencyDays: [10, 14], lowLightAdj: 3, highLightAdj: -1, brightDirectAdj: -1, outdoorAdj: -2, summerAdj: -2, winterAdj: 3,
    tips: ['当土壤完全干燥时浇水', '非常耐旱，过度浇水是主要杀手', '使用室温水'],
  },
  {
    id: 3, commonName: '虎皮兰', scientificName: 'Dracaena trifasciata', emoji: '🌱',
    baseFrequencyDays: [14, 21], lowLightAdj: 5, highLightAdj: -2, brightDirectAdj: -3, outdoorAdj: -3, summerAdj: -3, winterAdj: 7,
    tips: ['非常耐旱——少浇比多浇好', '确保花盆有排水孔', '冬季可以2-3周不浇水'],
  },
  {
    id: 4, commonName: '琴叶榕', scientificName: 'Ficus lyrata', emoji: '🌳',
    baseFrequencyDays: [7, 10], lowLightAdj: 4, highLightAdj: -1, brightDirectAdj: -2, outdoorAdj: -2, summerAdj: -2, winterAdj: 3,
    tips: ['彻底浇水直到水从底部流出', '保持一致的浇水时间表至关重要', '避免植物附近有冷风'],
  },
  {
    id: 5, commonName: '白掌', scientificName: 'Spathiphyllum wallisii', emoji: '🌼',
    baseFrequencyDays: [5, 7], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -1, outdoorAdj: -1, summerAdj: -1, winterAdj: 3,
    tips: ['戏剧性的垂头植物——浇水后立即恢复', '喜欢高湿度，经常给叶片喷雾', '保持土壤持续湿润但不积水'],
  },
  {
    id: 6, commonName: '芦荟', scientificName: 'Aloe barbadensis miller', emoji: '🌱',
    baseFrequencyDays: [14, 21], lowLightAdj: 5, highLightAdj: -2, brightDirectAdj: -4, outdoorAdj: -3, summerAdj: -4, winterAdj: 7,
    tips: ['深层但不频繁浇水', '让土壤在浇水之间完全干燥', '冬季大幅减少浇水'],
  },
  {
    id: 7, commonName: '吊兰', scientificName: 'Chlorophytum comosum', emoji: '🌿',
    baseFrequencyDays: [7, 10], lowLightAdj: 2, highLightAdj: 0, brightDirectAdj: -1, outdoorAdj: -1, summerAdj: -2, winterAdj: 3,
    tips: ['喜欢稍微湿润但不积水的土壤', '对自来水中的氟化物敏感——叶尖变棕请用过滤水', '偶尔忘记浇水也很宽容'],
  },
  {
    id: 8, commonName: '金钱树', scientificName: 'Zamioculcas zamiifolia', emoji: '🌱',
    baseFrequencyDays: [14, 21], lowLightAdj: 5, highLightAdj: -2, brightDirectAdj: -2, outdoorAdj: -3, summerAdj: -2, winterAdj: 7,
    tips: ['在根茎中储存水分——非常耐旱', '过度浇水是金钱树的头号杀手', '只在土壤完全干燥时浇水'],
  },
  {
    id: 9, commonName: '橡皮树', scientificName: 'Ficus elastica', emoji: '🌳',
    baseFrequencyDays: [7, 10], lowLightAdj: 3, highLightAdj: -1, brightDirectAdj: -2, outdoorAdj: -2, summerAdj: -2, winterAdj: 4,
    tips: ['用湿布擦拭叶片以保持光泽', '夏季保持土壤持续湿润，冬季更干燥', '下垂的叶子表示口渴'],
  },
  {
    id: 10, commonName: '波士顿蕨', scientificName: 'Nephrolepis exaltata', emoji: '🌿',
    baseFrequencyDays: [3, 5], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -1, outdoorAdj: -1, summerAdj: -1, winterAdj: 2,
    tips: ['保持土壤持续湿润，永远不要让它干燥', '需要60%以上的高湿度', '每天喷雾或使用加湿器'],
  },
  {
    id: 11, commonName: '玉树', scientificName: 'Crassula ovata', emoji: '🌳',
    baseFrequencyDays: [14, 21], lowLightAdj: 5, highLightAdj: -2, brightDirectAdj: -4, outdoorAdj: -3, summerAdj: -3, winterAdj: 7,
    tips: ['只在土壤完全干燥时浇水', '非常耐旱', '凉爽的夜晚促进开花'],
  },
  {
    id: 12, commonName: '薰衣草', scientificName: 'Lavandula angustifolia', emoji: '💜',
    baseFrequencyDays: [10, 14], lowLightAdj: 4, highLightAdj: -2, brightDirectAdj: -4, outdoorAdj: -3, summerAdj: -3, winterAdj: 5,
    tips: ['少量浇水，喜欢干燥条件', '过度浇水是致命的', '需要凉爽的冬季时期才能获得最佳花朵'],
  },
  {
    id: 13, commonName: '罗勒', scientificName: 'Ocimum basilicum', emoji: '🌿',
    baseFrequencyDays: [2, 3], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -1, outdoorAdj: -1, summerAdj: -1, winterAdj: 2,
    tips: ['保持土壤持续湿润但不积水', '在根部浇水，不要浇在叶子上', '定期掐尖促进茂密生长'],
  },
  {
    id: 14, commonName: '蝴蝶兰', scientificName: 'Phalaenopsis spp.', emoji: '🌸',
    baseFrequencyDays: [5, 7], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -2, outdoorAdj: -1, summerAdj: -1, winterAdj: 2,
    tips: ['当盆栽介质几乎干燥时浇水', '使用温水，永远不要用冷水', '冰块法：每周3个冰块效果很好'],
  },
  {
    id: 15, commonName: '竹芋', scientificName: 'Calathea spp.', emoji: '🌿',
    baseFrequencyDays: [5, 7], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -2, outdoorAdj: -2, summerAdj: -1, winterAdj: 3,
    tips: ['保持土壤持续湿润但不要积水', '使用过滤或蒸馏水——对化学物质敏感', '高湿度对防止叶片边缘变脆至关重要'],
  },
  {
    id: 16, commonName: '天堂鸟', scientificName: 'Strelitzia reginae', emoji: '🦜',
    baseFrequencyDays: [7, 10], lowLightAdj: 3, highLightAdj: -1, brightDirectAdj: -2, outdoorAdj: -2, summerAdj: -2, winterAdj: 3,
    tips: ['当土壤顶部2英寸干燥时浇水', '喜欢充分浸泡后干燥', '给叶片喷雾以增加热带湿度'],
  },
  {
    id: 17, commonName: '佛珠', scientificName: 'Curio rowleyanus', emoji: '📿',
    baseFrequencyDays: [14, 21], lowLightAdj: 5, highLightAdj: -2, brightDirectAdj: -3, outdoorAdj: -3, summerAdj: -3, winterAdj: 7,
    tips: ['只在珠子开始看起来稍微放气时浇水', '非常耐旱', '沙质、排水极好的仙人掌混合土'],
  },
  {
    id: 18, commonName: '心叶蔓绿绒', scientificName: 'Philodendron hederaceum', emoji: '💚',
    baseFrequencyDays: [7, 10], lowLightAdj: 3, highLightAdj: -1, brightDirectAdj: -1, outdoorAdj: -2, summerAdj: -2, winterAdj: 3,
    tips: ['当土壤顶部1英寸干燥时浇水', '对偶尔的疏忽很宽容', '适应性很强，低光到明亮间接光下都茁壮成长'],
  },
  {
    id: 19, commonName: '镜面草', scientificName: 'Pilea peperomioides', emoji: '🪙',
    baseFrequencyDays: [7, 10], lowLightAdj: 2, highLightAdj: 0, brightDirectAdj: -1, outdoorAdj: -1, summerAdj: -2, winterAdj: 3,
    tips: ['当土壤顶部1英寸干燥时浇水', '定期旋转以保证均匀生长', '避免直射阳光'],
  },
  {
    id: 20, commonName: '仙人掌', scientificName: 'Cactaceae spp.', emoji: '🌵',
    baseFrequencyDays: [21, 30], lowLightAdj: 5, highLightAdj: -3, brightDirectAdj: -5, outdoorAdj: -5, summerAdj: -3, winterAdj: 10,
    tips: ['非常少量浇水，让土壤完全干燥', '夏季每月一次，冬季几乎不浇水', '需要最大光照'],
  },
  {
    id: 21, commonName: '散尾葵', scientificName: 'Dypsis lutescens', emoji: '🌴',
    baseFrequencyDays: [5, 8], lowLightAdj: 3, highLightAdj: -1, brightDirectAdj: -2, outdoorAdj: -2, summerAdj: -1, winterAdj: 3,
    tips: ['保持土壤均匀湿润但不要积水', '定期给叶片喷雾增加湿度', '确保排水良好避免积水'],
  },
  {
    id: 22, commonName: '春羽', scientificName: 'Thaumatophyllum bipinnatifidum', emoji: '🌿',
    baseFrequencyDays: [5, 8], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -2, outdoorAdj: -2, summerAdj: -1, winterAdj: 3,
    tips: ['浇水前检查表土2-3厘米', '春夏保持土壤湿润，秋冬等表土干燥后浇水', '定期喷雾增加湿度'],
  },
  {
    id: 23, commonName: '铜钱草', scientificName: 'Hydrocotyle vulgaris', emoji: '🍀',
    baseFrequencyDays: [2, 4], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -1, outdoorAdj: -1, summerAdj: -1, winterAdj: 2,
    tips: ['保持土壤始终湿润', '可以完全水培', '喜欢高湿度环境'],
  },
  {
    id: 24, commonName: '幸福树', scientificName: 'Radermachera sinica', emoji: '🌳',
    baseFrequencyDays: [7, 10], lowLightAdj: 3, highLightAdj: -1, brightDirectAdj: -1, outdoorAdj: -2, summerAdj: -2, winterAdj: 3,
    tips: ['当土壤顶部干燥时浇水', '避免积水', '喜欢中等至高湿度，定期喷雾'],
  },
  {
    id: 25, commonName: '网纹草', scientificName: 'Fittonia spp.', emoji: '🌿',
    baseFrequencyDays: [3, 5], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -1, outdoorAdj: -1, summerAdj: -1, winterAdj: 2,
    tips: ['保持土壤持续湿润', '对干燥非常敏感', '需要高湿度，非常适合 terrarium'],
  },
  {
    id: 26, commonName: '袖珍椰子', scientificName: 'Chamaedorea elegans', emoji: '🌴',
    baseFrequencyDays: [5, 8], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -1, outdoorAdj: -1, summerAdj: -1, winterAdj: 3,
    tips: ['保持土壤微湿', '避免积水', '耐阴性好，适合低光环境'],
  },
  {
    id: 27, commonName: '豆瓣绿', scientificName: 'Peperomia obtusifolia', emoji: '🌱',
    baseFrequencyDays: [7, 10], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -1, outdoorAdj: -1, summerAdj: -2, winterAdj: 3,
    tips: ['浇水要浇透，但避免积水', '土壤表层干燥后浇水', '耐受正常家庭湿度'],
  },
  {
    id: 28, commonName: '花叶万年青', scientificName: 'Dieffenbachia spp.', emoji: '🌿',
    baseFrequencyDays: [7, 10], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -1, outdoorAdj: -1, summerAdj: -2, winterAdj: 3,
    tips: ['保持土壤微湿', '避免积水', '注意：汁液有毒，避免接触'],
  },
  {
    id: 29, commonName: '酒瓶兰', scientificName: 'Beaucarnea recurvata', emoji: '🌴',
    baseFrequencyDays: [14, 21], lowLightAdj: 5, highLightAdj: -2, brightDirectAdj: -4, outdoorAdj: -3, summerAdj: -3, winterAdj: 7,
    tips: ['深层但不频繁浇水', '让土壤完全干燥', '瓶状茎干储存水分，几乎杀不死'],
  },
  {
    id: 30, commonName: '合果芋', scientificName: 'Syngonium podophyllum', emoji: '🌿',
    baseFrequencyDays: [5, 8], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -1, outdoorAdj: -1, summerAdj: -1, winterAdj: 3,
    tips: ['保持土壤微湿', '避免积水', '喜欢高湿度，定期喷雾'],
  },
  {
    id: 31, commonName: '绿宝石', scientificName: 'Scindapsus pictus', emoji: '💎',
    baseFrequencyDays: [7, 12], lowLightAdj: 3, highLightAdj: -1, brightDirectAdj: -1, outdoorAdj: -2, summerAdj: -2, winterAdj: 3,
    tips: ['当土壤干燥时浇水', '耐旱性好', '喜欢低到中等间接光'],
  },
  {
    id: 32, commonName: '白雪公主', scientificName: 'Aglaonema commutatum', emoji: '🤍',
    baseFrequencyDays: [7, 10], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -1, outdoorAdj: -1, summerAdj: -2, winterAdj: 3,
    tips: ['保持土壤微湿', '避免积水', '极度耐阴，是低光照环境的理想选择'],
  },
  {
    id: 33, commonName: '龙血树', scientificName: 'Dracaena marginata', emoji: '🐉',
    baseFrequencyDays: [10, 14], lowLightAdj: 3, highLightAdj: -1, brightDirectAdj: -2, outdoorAdj: -2, summerAdj: -2, winterAdj: 5,
    tips: ['当土壤干燥时浇水', '耐旱性好', '耐阴性好，适合低光环境'],
  },
  {
    id: 34, commonName: '仙客来', scientificName: 'Cyclamen persicum', emoji: '🌸',
    baseFrequencyDays: [5, 7], lowLightAdj: 2, highLightAdj: 0, brightDirectAdj: -1, outdoorAdj: -1, summerAdj: 1, winterAdj: 2,
    tips: ['从下方托盘浇水，避免浇到球茎顶部', '水温保持室温', '避免水接触叶片和花茎'],
  },
  {
    id: 35, commonName: '倒挂金钟', scientificName: 'Fuchsia spp.', emoji: '🏮',
    baseFrequencyDays: [5, 7], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -1, outdoorAdj: -1, summerAdj: 0, winterAdj: 3,
    tips: ['保持土壤湿润但不积水', '避免让土壤完全干燥', '高温导致生长不良，喜凉爽环境'],
  },
  {
    id: 36, commonName: '马醉木', scientificName: 'Pieris japonica', emoji: '🔔',
    baseFrequencyDays: [6, 10], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -2, outdoorAdj: -1, summerAdj: -2, winterAdj: 3,
    tips: ['土培遵循见干见湿，表土彻底干燥再浇透', '水养枝条每4-6天换干净清水', '害怕盛夏酷暑闷热天气'],
  },
  {
    id: 37, commonName: '油画婚礼紫露草', scientificName: 'Tradescantia fluminensis Quadricolor', emoji: '🎨',
    baseFrequencyDays: [4, 7], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -2, outdoorAdj: -1, summerAdj: -1, winterAdj: 3,
    tips: ['等盆土表面往下2-3厘米彻底变干后再浇透', '冬季严格控水', '充足光照是维持梦幻粉色的核心条件'],
  },
  {
    id: 38, commonName: '琴叶喜林芋', scientificName: 'Philodendron panduriforme', emoji: '🎻',
    baseFrequencyDays: [5, 8], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -2, outdoorAdj: -1, summerAdj: -1, winterAdj: 3,
    tips: ['等盆土表层往下3-4厘米彻底变干后再浇透', '冬季必须严格控水偏干', '对空气湿度要求较高'],
  },
  {
    id: 39, commonName: '青叶冷水花', scientificName: 'Pilea cadierei', emoji: '❄️',
    baseFrequencyDays: [3, 5], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -1, outdoorAdj: -1, summerAdj: -1, winterAdj: 2,
    tips: ['生长期保持盆土充沛湿润', '表土微干就要立即浇透水', '在干燥季节需高频喷雾以防叶尖枯焦'],
  },
  {
    id: 40, commonName: '鸟巢蕨', scientificName: 'Asplenium nidus', emoji: '🪹',
    baseFrequencyDays: [3, 5], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -1, outdoorAdj: -1, summerAdj: -1, winterAdj: 2,
    tips: ['大叶片水分蒸发快，生长期必须保持盆土彻底湿润', '需要高湿度，空气干燥会导致叶缘干焦', '高度耐阴，极忌阳光暴晒'],
  },
  {
    id: 41, commonName: '黑叶观音莲', scientificName: 'Alocasia amazonica', emoji: '🖤',
    baseFrequencyDays: [5, 8], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -2, outdoorAdj: -1, summerAdj: -1, winterAdj: 3,
    tips: ['遵循见干见湿原则', '等盆土表层往下2-3厘米彻底变干再浇透', '理想湿度在65%以上'],
  },
  {
    id: 42, commonName: '金钻蔓绿绒', scientificName: 'Philodendron Imperial Green', emoji: '💚',
    baseFrequencyDays: [5, 8], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -2, outdoorAdj: -1, summerAdj: -1, winterAdj: 3,
    tips: ['浇水遵循见干见湿原则', '等盆土表层往下3-4厘米彻底变干再浇透', '需定期喷雾保持湿度'],
  },
  {
    id: 43, commonName: '千手观音海芋', scientificName: 'Alocasia cucullata', emoji: '🙏',
    baseFrequencyDays: [4, 7], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -2, outdoorAdj: -1, summerAdj: -1, winterAdj: 3,
    tips: ['属于喜水派，生长期必须保持盆土充沛湿润', '对空气湿度要求较高', '严禁盛夏烈日直射曝晒'],
  },
  {
    id: 44, commonName: '彩叶芋', scientificName: 'Caladium bicolor', emoji: '🦋',
    baseFrequencyDays: [2, 4], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -2, outdoorAdj: -1, summerAdj: -1, winterAdj: 2,
    tips: ['著名的水娃娃，叶片极薄不耐任何形式的干旱', '理想湿度在65%-80%', '具有冬眠特性，冬季需控水'],
  },
  {
    id: 45, commonName: '常春藤', scientificName: 'Hedera helix', emoji: '🍀',
    baseFrequencyDays: [5, 8], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -1, outdoorAdj: -1, summerAdj: -1, winterAdj: 2,
    tips: ['最怕土壤长期处于湿漉漉的状态', '革质叶片蒸腾较慢，具有较强抗旱能力', '抗寒耐寒能力极强'],
  },
  {
    id: 46, commonName: '九里香', scientificName: 'Murraya exotica', emoji: '🌸',
    baseFrequencyDays: [5, 8], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -2, outdoorAdj: -1, summerAdj: -1, winterAdj: 3,
    tips: ['木本植物主干储水能力强，具有一定耐旱性', '根系最怕长期泡水', '阳光越充沛花香越浓郁'],
  },
  {
    id: 47, commonName: '向日葵', scientificName: 'Helianthus annuus', emoji: '🌻',
    baseFrequencyDays: [1, 3], lowLightAdj: -1, highLightAdj: 0, brightDirectAdj: -1, outdoorAdj: -1, summerAdj: -1, winterAdj: 2,
    tips: ['盛夏生长期蒸腾量极大，必须保持盆土湿润', '严忌花盆长期积水闷根', '一年生植物冬季已枯死无需浇水'],
  },
  {
    id: 48, commonName: '薄荷', scientificName: 'Mentha canadensis', emoji: '🌿',
    baseFrequencyDays: [2, 4], lowLightAdj: 1, highLightAdj: 0, brightDirectAdj: -1, outdoorAdj: -1, summerAdj: -1, winterAdj: 3,
    tips: ['薄荷属于"喝水狂魔"，不耐任何干旱', '表土一变干就必须立刻浇透水', '冬季严格控水防冻伤'],
  },
  {
    id: 49, commonName: '无尽夏', scientificName: 'Hydrangea macrophylla Endless Summer', emoji: '💐',
    baseFrequencyDays: [2, 4], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -1, outdoorAdj: -1, summerAdj: -1, winterAdj: 3,
    tips: ['花卉界著名的"水老虎"，极度不耐干旱', '缺水花球会在几小时内软瘫低垂', '冬季冬眠期严格控水'],
  },
  {
    id: 50, commonName: '多肉吉娃娃', scientificName: 'Echeveria chihuahuensis', emoji: '🪴',
    baseFrequencyDays: [10, 20], lowLightAdj: 5, highLightAdj: -3, brightDirectAdj: -5, outdoorAdj: -3, summerAdj: -3, winterAdj: 10,
    tips: ['严格遵循"宁干勿湿"、"干透浇透"', '严禁将水灌入中心莲座叶盘', '夏季高温半休眠必须严格控水'],
  },
  {
    id: 51, commonName: '文心兰', scientificName: 'Oncidium flexuosum', emoji: '💃',
    baseFrequencyDays: [5, 8], lowLightAdj: 2, highLightAdj: -1, brightDirectAdj: -2, outdoorAdj: -1, summerAdj: -1, winterAdj: 3,
    tips: ['看水苔颜色和气生根变白时再浇透', '严忌普通泥土栽种', '冬季低温严格控水'],
  },
]

const potConfigs = computed<Record<PotSize, { label: string; range: string; icon: number; waterAdj: number }>>(() => {
  const sizes = t('watering.potSizes') as unknown as string[]
  return {
    small:  { label: sizes[0] || 'Small',       range: '10-15 cm',  icon: 0.6,  waterAdj: -2 },
    medium: { label: sizes[1] || 'Medium',      range: '15-25 cm',  icon: 1.0,  waterAdj: 0  },
    large:  { label: sizes[2] || 'Large',       range: '25-40 cm',  icon: 1.4,  waterAdj: 2  },
    xlarge: { label: sizes[3] || 'Extra Large', range: '40 cm+',    icon: 1.8,  waterAdj: 4  },
  }
})

const potSizes: PotSize[] = ['small', 'medium', 'large', 'xlarge']

const lightOptions = computed<{ value: LightLevel; label: string; icon: string }[]>(() => [
  { value: 'low',           label: t('encyclopedia.lowLight'),     icon: '☀️' },
  { value: 'medium',        label: t('encyclopedia.mediumLight'),   icon: '🌤️' },
  { value: 'high',          label: t('encyclopedia.brightLight'),   icon: '☀️' },
  { value: 'bright_direct', label: t('encyclopedia.directSun'),      icon: '🔆' },
])

const seasons = computed<{ value: Season; label: string; icon: string }[]>(() => [
  { value: 'spring', label: t('watering.spring'), icon: '🌸' },
  { value: 'summer', label: t('watering.summer'), icon: '☀️' },
  { value: 'fall',   label: t('watering.fall'),   icon: '🍂' },
  { value: 'winter', label: t('watering.winter'), icon: '❄️' },
])

// ─── State ──────────────────────────────────────────────────
const currentStep = ref(1)
const totalSteps = 4
const direction = ref<'left' | 'right'>('left')
const searchQuery = ref('')
const selectedPlant = ref<CalcPlant | null>(null)
const potSize = ref<PotSize>('medium')
const potSliderValue = ref(1)   // 0-3 mapping to potSizes index
const environment = ref<Environment>('indoor')
const lightLevel = ref<LightLevel>('medium')
const season = ref<Season>('spring')
const resultVisible = ref(false)

// ─── Computed ───────────────────────────────────────────────
const filteredPlants = computed(() => {
  if (!searchQuery.value.trim()) return mockPlants
  const q = searchQuery.value.toLowerCase()
  return mockPlants.filter(
    (p) =>
      p.commonName.toLowerCase().includes(q) ||
      p.scientificName.toLowerCase().includes(q),
  )
})

const potScale = computed(() => {
  const cfg = potConfigs.value[potSize.value]
  return cfg.icon
})

const wateringResult = computed(() => {
  if (!selectedPlant.value) return null
  const plant = selectedPlant.value

  let minDays = plant.baseFrequencyDays[0]
  let maxDays = plant.baseFrequencyDays[1]

  // Pot size adjustment
  const potAdj = potConfigs.value[potSize.value].waterAdj
  minDays += potAdj
  maxDays += potAdj

  // Light adjustment
  if (lightLevel.value === 'low') { minDays += plant.lowLightAdj; maxDays += plant.lowLightAdj }
  else if (lightLevel.value === 'high') { minDays += plant.highLightAdj; maxDays += plant.highLightAdj }
  else if (lightLevel.value === 'bright_direct') { minDays += plant.brightDirectAdj; maxDays += plant.brightDirectAdj }

  // Outdoor adjustment
  if (environment.value === 'outdoor') { minDays += plant.outdoorAdj; maxDays += plant.outdoorAdj }

  // Season adjustment
  if (season.value === 'summer') { minDays += plant.summerAdj; maxDays += plant.summerAdj }
  else if (season.value === 'winter') { minDays += plant.winterAdj; maxDays += plant.winterAdj }

  // Clamp to sensible range
  minDays = Math.max(1, Math.round(minDays))
  maxDays = Math.max(minDays + 1, Math.round(maxDays))

  // Build calendar (30 days)
  const avgInterval = Math.round((minDays + maxDays) / 2)
  const wateringDays: number[] = []
  let nextWater = 1
  while (nextWater <= 30) {
    wateringDays.push(nextWater)
    nextWater += avgInterval
  }

  return { minDays, maxDays, wateringDays, avgInterval }
})

const canGoNext = computed(() => {
  if (currentStep.value === 1) return !!selectedPlant.value
  if (currentStep.value === 2) return true
  if (currentStep.value === 3) return true
  return false
})

// ─── Watchers ───────────────────────────────────────────────
watch(potSliderValue, (val) => {
  potSize.value = potSizes[val]
})

// ─── Methods ────────────────────────────────────────────────
function selectPlant(plant: CalcPlant) {
  selectedPlant.value = plant
}

function goNext() {
  if (!canGoNext.value) return
  direction.value = 'left'
  if (currentStep.value < totalSteps) {
    currentStep.value++
  }
  if (currentStep.value === 4) {
    nextTick(() => {
      setTimeout(() => { resultVisible.value = true }, 200)
    })
  }
}

function goBack() {
  direction.value = 'right'
  if (currentStep.value > 1) {
    currentStep.value--
  }
  if (currentStep.value !== 4) {
    resultVisible.value = false
  }
}

function startOver() {
  direction.value = 'right'
  resultVisible.value = false
  selectedPlant.value = null
  potSize.value = 'medium'
  potSliderValue.value = 1
  environment.value = 'indoor'
  lightLevel.value = 'medium'
  season.value = 'spring'
  searchQuery.value = ''
  currentStep.value = 1
}

// ─── Calendar helpers ───────────────────────────────────────
function getMonthName(): string {
  const seasonMonthMap: Record<Season, number> = { spring: 3, summer: 6, fall: 9, winter: 0 }
  const d = new Date(2026, seasonMonthMap[season.value], 1)
  return d.toLocaleString('default', { month: 'long' })
}

function getDaysInMonth(): number {
  return 30
}
</script>

<template>
  <div class="watering-calc">
    <!-- ── Header ──────────────────────────────────────────── -->
    <div class="watering-calc__header">
      <h1 class="watering-calc__title">{{ $t('watering.title') }}</h1>
      <p class="watering-calc__subtitle">{{ $t('watering.subtitle') }}</p>
    </div>

    <!-- ── Progress bar ────────────────────────────────────── -->
    <div class="watering-calc__progress">
      <div class="watering-calc__progress-track">
        <div
          class="watering-calc__progress-fill"
          :style="{ width: `${(currentStep / totalSteps) * 100}%` }"
        />
      </div>
      <div class="watering-calc__progress-steps">
        <button
          v-for="s in totalSteps"
          :key="s"
          class="watering-calc__progress-dot"
          :class="{
            'watering-calc__progress-dot--active': s === currentStep,
            'watering-calc__progress-dot--done': s < currentStep,
          }"
          :disabled="s > currentStep"
          @click="s < currentStep ? ((direction = 'right'), (currentStep = s), (resultVisible = false)) : null"
        >
          <span v-if="s < currentStep" class="watering-calc__progress-check">
            <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M3.5 8.5L6.5 11.5L12.5 4.5" />
            </svg>
          </span>
          <span v-else>{{ s }}</span>
        </button>
      </div>
      <p class="watering-calc__progress-label">{{ $t('watering.step' + currentStep) }} ({{ currentStep }}/{{ totalSteps }})</p>
    </div>

    <!-- ── Wizard container ────────────────────────────────── -->
    <div class="watering-calc__wizard">
      <Transition :name="direction === 'left' ? 'slide-left' : 'slide-right'" mode="out-in">

        <!-- ===== STEP 1 — Select Plant ===== -->
        <div v-if="currentStep === 1" key="step1" class="watering-calc__step">
          <h2 class="watering-calc__step-title">{{ $t('watering.step1') }}</h2>
          <p class="watering-calc__step-desc">{{ $t('watering.searchPlant') }}</p>

          <div class="watering-calc__search">
            <svg class="watering-calc__search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8" />
              <path d="m21 21-4.3-4.3" />
            </svg>
            <input
              v-model="searchQuery"
              type="text"
              :placeholder="$t('watering.searchPlant')"
              class="watering-calc__search-input"
            />
          </div>

          <div class="watering-calc__plant-grid">
            <button
              v-for="plant in filteredPlants"
              :key="plant.id"
              class="watering-calc__plant-card"
              :class="{ 'watering-calc__plant-card--selected': selectedPlant?.id === plant.id }"
              @click="selectPlant(plant)"
            >
              <span class="watering-calc__plant-emoji">{{ plant.emoji }}</span>
              <span class="watering-calc__plant-name">{{ plant.commonName }}</span>
              <span class="watering-calc__plant-sci">{{ plant.scientificName }}</span>
            </button>
          </div>

          <p v-if="filteredPlants.length === 0" class="watering-calc__empty">
            {{ $t('encyclopedia.noResults') }}
          </p>
        </div>

        <!-- ===== STEP 2 — Pot Size ===== -->
        <div v-else-if="currentStep === 2" key="step2" class="watering-calc__step">
          <h2 class="watering-calc__step-title">{{ $t('watering.step2') }}</h2>
          <p class="watering-calc__step-desc">{{ $t('watering.potSize') }}</p>

          <div class="watering-calc__pot-area">
            <!-- SVG pot icon -->
            <div class="watering-calc__pot-visual">
              <svg
                class="watering-calc__pot-svg"
                :style="{ transform: `scale(${potScale})` }"
                viewBox="0 0 120 140"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <!-- Pot rim -->
                <rect x="10" y="20" width="100" height="14" rx="7" fill="#c08a50" />
                <!-- Pot body -->
                <path d="M16 34 L24 120 C24 128 96 128 96 120 L104 34 Z" fill="#d4a574" />
                <!-- Pot rim highlight -->
                <rect x="10" y="20" width="100" height="5" rx="2.5" fill="#e8cba0" opacity="0.6" />
                <!-- Soil -->
                <ellipse cx="60" cy="34" rx="44" ry="6" fill="#6b4828" />
                <!-- Plant sprout -->
                <path d="M60 34 C60 14 42 2 50 -8 C58 -2 60 10 60 20" stroke="#16a34a" stroke-width="2.5" fill="none" stroke-linecap="round" />
                <path d="M60 24 C70 14 86 16 80 26 C74 20 62 20 60 24Z" fill="#22c55e" />
                <path d="M60 18 C48 8 32 14 40 22 C46 16 58 14 60 18Z" fill="#4ade80" />
                <!-- Drainage hole shadow -->
                <ellipse cx="60" cy="124" rx="8" ry="3" fill="#a0703c" opacity="0.4" />
              </svg>
            </div>

            <!-- Slider -->
            <div class="watering-calc__pot-slider-wrap">
              <input
                v-model.number="potSliderValue"
                type="range"
                min="0"
                max="3"
                step="1"
                class="watering-calc__pot-slider"
              />
              <div class="watering-calc__pot-labels">
                <span
                  v-for="(size, idx) in potSizes"
                  :key="size"
                  class="watering-calc__pot-label"
                  :class="{ 'watering-calc__pot-label--active': potSliderValue === idx }"
                  @click="potSliderValue = idx"
                >
                  {{ potConfigs[size].label }}
                </span>
              </div>
            </div>

            <!-- Current size info -->
            <div class="watering-calc__pot-info">
              <span class="watering-calc__pot-info-label">{{ potConfigs[potSize]?.label }}</span>
              <span class="watering-calc__pot-info-range">{{ potConfigs[potSize]?.range }}</span>
            </div>
          </div>
        </div>

        <!-- ===== STEP 3 — Environment ===== -->
        <div v-else-if="currentStep === 3" key="step3" class="watering-calc__step">
          <h2 class="watering-calc__step-title">{{ $t('watering.step3') }}</h2>
          <p class="watering-calc__step-desc">{{ $t('watering.conditions') }}</p>

          <div class="watering-calc__env-sections">
            <!-- Indoor / Outdoor -->
            <div class="watering-calc__env-group">
              <label class="watering-calc__env-label">{{ $t('watering.indoor') }} / {{ $t('watering.outdoor') }}</label>
              <div class="watering-calc__toggle-row">
                <button
                  class="watering-calc__toggle-btn"
                  :class="{ 'watering-calc__toggle-btn--active': environment === 'indoor' }"
                  @click="environment = 'indoor'"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="watering-calc__toggle-icon">
                    <path d="M3 12l9-9 9 9" stroke-linecap="round" stroke-linejoin="round" />
                    <path d="M5 10v9a1 1 0 001 1h3v-5h6v5h3a1 1 0 001-1v-9" stroke-linecap="round" stroke-linejoin="round" />
                  </svg>
                  {{ $t('watering.indoor') }}
                </button>
                <button
                  class="watering-calc__toggle-btn"
                  :class="{ 'watering-calc__toggle-btn--active': environment === 'outdoor' }"
                  @click="environment = 'outdoor'"
                >
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="watering-calc__toggle-icon">
                    <circle cx="12" cy="12" r="4" />
                    <path d="M12 2v2m0 16v2m-10-10h2m16 0h2m-3.5-6.5l-1.4 1.4M6.9 17.1l-1.4 1.4m0-13l1.4 1.4m10.2 10.2l1.4 1.4" stroke-linecap="round" />
                  </svg>
                  {{ $t('watering.outdoor') }}
                </button>
              </div>
            </div>

            <!-- Light level -->
            <div class="watering-calc__env-group">
              <label class="watering-calc__env-label">{{ $t('watering.lightLevel') }}</label>
              <div class="watering-calc__light-grid">
                <button
                  v-for="opt in lightOptions"
                  :key="opt.value"
                  class="watering-calc__light-btn"
                  :class="{ 'watering-calc__light-btn--active': lightLevel === opt.value }"
                  @click="lightLevel = opt.value"
                >
                  <span class="watering-calc__light-icon">{{ opt.icon }}</span>
                  <span class="watering-calc__light-text">{{ opt.label }}</span>
                </button>
              </div>
            </div>

            <!-- Season -->
            <div class="watering-calc__env-group">
              <label class="watering-calc__env-label">{{ $t('watering.season') }}</label>
              <div class="watering-calc__season-row">
                <button
                  v-for="s in seasons"
                  :key="s.value"
                  class="watering-calc__season-btn"
                  :class="{ 'watering-calc__season-btn--active': season === s.value }"
                  @click="season = s.value"
                >
                  <span class="watering-calc__season-icon">{{ s.icon }}</span>
                  <span class="watering-calc__season-text">{{ s.label }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- ===== STEP 4 — Result ===== -->
        <div v-else-if="currentStep === 4" key="step4" class="watering-calc__step watering-calc__step--result">
          <div
            class="watering-calc__result"
            :class="{ 'watering-calc__result--visible': resultVisible }"
          >
            <!-- Plant summary -->
            <div class="watering-calc__result-header">
              <span class="watering-calc__result-emoji">{{ selectedPlant?.emoji }}</span>
              <div>
                <h2 class="watering-calc__result-plant-name">{{ selectedPlant?.commonName }}</h2>
                <p class="watering-calc__result-plant-sci">{{ selectedPlant?.scientificName }}</p>
              </div>
            </div>

            <!-- Frequency card -->
            <div class="watering-calc__result-card watering-calc__result-card--frequency">
              <div class="watering-calc__result-card-icon">
                <svg viewBox="0 0 48 48" fill="none">
                  <path d="M24 6C24 6 10 18 10 28C10 35.732 16.268 42 24 42C31.732 42 38 35.732 38 28C38 18 24 6 24 6Z" fill="url(#waterGrad)" />
                  <defs>
                    <linearGradient id="waterGrad" x1="10" y1="6" x2="38" y2="42">
                      <stop stop-color="#93c5fd" />
                      <stop offset="1" stop-color="#2563eb" />
                    </linearGradient>
                  </defs>
                </svg>
              </div>
              <div class="watering-calc__result-card-content">
                <p class="watering-calc__result-card-label">{{ $t('watering.frequency') }}</p>
                <p class="watering-calc__result-card-value">
                  {{ $t('watering.everyDays', { min: wateringResult?.minDays, max: wateringResult?.maxDays }) }}
                </p>
                <p class="watering-calc__result-card-detail">
                  Approximately once every {{ wateringResult?.avgInterval }} days
                </p>
              </div>
            </div>

            <!-- Calendar -->
            <div class="watering-calc__result-card watering-calc__result-card--calendar">
              <p class="watering-calc__result-card-label">{{ getMonthName() }} {{ $t('watering.calendar') }}</p>
              <div class="watering-calc__calendar">
                <span
                  v-for="day in getDaysInMonth()"
                  :key="day"
                  class="watering-calc__calendar-day"
                  :class="{
                    'watering-calc__calendar-day--water': wateringResult?.wateringDays.includes(day),
                  }"
                >
                  {{ day }}
                </span>
              </div>
              <div class="watering-calc__calendar-legend">
                <span class="watering-calc__calendar-legend-item">
                  <span class="watering-calc__calendar-legend-dot watering-calc__calendar-legend-dot--water" />
                  {{ $t('watering.nextWater') }}
                </span>
                <span class="watering-calc__calendar-legend-item">
                  <span class="watering-calc__calendar-legend-dot" />
                  {{ $t('watering.lastWatered') }}
                </span>
              </div>
            </div>

            <!-- Tips -->
            <div class="watering-calc__result-card watering-calc__result-card--tips">
              <p class="watering-calc__result-card-label">
                <svg viewBox="0 0 20 20" fill="currentColor" class="watering-calc__tip-icon">
                  <path d="M10 2a6 6 0 00-3.2 11.07c.35.2.6.53.6.93v1a1 1 0 001 1h3.2a1 1 0 001-1v-1c0-.4.25-.73.6-.93A6 6 0 0010 2zm-1 15h2v1h-2v-1zm1-11a1 1 0 011 1v2a1 1 0 11-2 0V7a1 1 0 011-1z" />
                </svg>
                {{ $t('watering.tips') }}
              </p>
              <ul class="watering-calc__tips-list">
                <li
                  v-for="(tip, idx) in selectedPlant?.tips"
                  :key="idx"
                  class="watering-calc__tip-item"
                  :style="{ animationDelay: `${0.6 + idx * 0.12}s` }"
                >
                  {{ tip }}
                </li>
              </ul>
            </div>

            <!-- Conditions summary -->
            <div class="watering-calc__conditions-summary">
              <span class="watering-calc__condition-tag">
                {{ potConfigs[potSize]?.label }} ({{ potConfigs[potSize]?.range }})
              </span>
              <span class="watering-calc__condition-tag">
                {{ environment === 'indoor' ? $t('watering.indoor') : $t('watering.outdoor') }}
              </span>
              <span class="watering-calc__condition-tag">
                {{ lightOptions.find(l => l.value === lightLevel)?.label }}
              </span>
              <span class="watering-calc__condition-tag">
                {{ seasons.find(s => s.value === season)?.label }}
              </span>
            </div>

            <!-- Start Over -->
            <button class="watering-calc__start-over" @click="startOver">
              <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M3 10a7 7 0 0112.9-3.7M17 10a7 7 0 01-12.9 3.7" />
                <path d="M16 3v4h-4M4 17v-4h4" />
              </svg>
              {{ $t('watering.startOver') }}
            </button>
          </div>
        </div>
      </Transition>
    </div>

    <!-- ── Navigation buttons ──────────────────────────────── -->
    <div v-if="currentStep < 4" class="watering-calc__nav">
      <button
        v-if="currentStep > 1"
        class="watering-calc__nav-btn watering-calc__nav-btn--back"
        @click="goBack"
      >
        <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 4l-6 6 6 6" />
        </svg>
        {{ $t('watering.back') }}
      </button>
      <div v-else />
      <button
        class="watering-calc__nav-btn watering-calc__nav-btn--next"
        :disabled="!canGoNext"
        @click="goNext"
      >
        {{ $t('watering.next') }}
        <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M8 4l6 6-6 6" />
        </svg>
      </button>
    </div>
  </div>
</template>

<style scoped lang="scss">
// ─── Transitions ─────────────────────────────────────────────
.slide-left-enter-active,
.slide-left-leave-active,
.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.35s $ease-out-expo;
}

.slide-left-enter-from {
  opacity: 0;
  transform: translateX(60px);
}
.slide-left-leave-to {
  opacity: 0;
  transform: translateX(-60px);
}

.slide-right-enter-from {
  opacity: 0;
  transform: translateX(-60px);
}
.slide-right-leave-to {
  opacity: 0;
  transform: translateX(60px);
}

// ─── Component ──────────────────────────────────────────────
.watering-calc {
  min-height: 100vh;
  padding: 2rem 1.5rem 3rem;
  max-width: 720px;
  margin: 0 auto;

  // ── Header ────────────────────────────────────────────────
  &__header {
    text-align: center;
    margin-bottom: 2rem;
  }

  &__title {
    font-family: $font-display;
    font-size: clamp(1.75rem, 4vw, 2.5rem);
    color: $color-leaf-800;
    margin-bottom: 0.375rem;
  }

  &__subtitle {
    font-size: $font-size-base;
    color: $color-text-muted;
  }

  // ── Progress bar ──────────────────────────────────────────
  &__progress {
    margin-bottom: 2.5rem;
  }

  &__progress-track {
    height: 4px;
    background: $color-border;
    border-radius: 2px;
    overflow: hidden;
    margin-bottom: 1rem;
  }

  &__progress-fill {
    height: 100%;
    background: linear-gradient(90deg, $color-leaf-400, $color-leaf-600);
    border-radius: 2px;
    transition: width 0.5s $ease-out-expo;
  }

  &__progress-steps {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 0.5rem;
  }

  &__progress-dot {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    border: 2px solid $color-border;
    background: $color-surface;
    color: $color-text-muted;
    font-size: $font-size-sm;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s $ease-spring;
    cursor: default;

    &--active {
      border-color: $color-leaf-500;
      background: $color-leaf-500;
      color: white;
      box-shadow: 0 0 0 4px rgba($color-leaf-500, 0.15);
    }

    &--done {
      border-color: $color-leaf-400;
      background: $color-leaf-400;
      color: white;
      cursor: pointer;

      &:hover {
        background: $color-leaf-500;
      }
    }
  }

  &__progress-check {
    display: flex;
    width: 16px;
    height: 16px;
  }

  &__progress-label {
    text-align: center;
    font-size: $font-size-xs;
    color: $color-text-muted;
    margin-top: 0.5rem;
  }

  // ── Wizard ────────────────────────────────────────────────
  &__wizard {
    position: relative;
    overflow: hidden;
  }

  &__step {
    &-title {
      font-family: $font-display;
      font-size: $font-size-2xl;
      color: $color-leaf-800;
      margin-bottom: 0.25rem;
    }

    &-desc {
      color: $color-text-muted;
      margin-bottom: 1.5rem;
    }
  }

  // ── Step 1 — Search & plant grid ──────────────────────────
  &__search {
    position: relative;
    margin-bottom: 1.5rem;
  }

  &__search-icon {
    position: absolute;
    left: 1rem;
    top: 50%;
    transform: translateY(-50%);
    width: 18px;
    height: 18px;
    color: $color-text-muted;
  }

  &__search-input {
    width: 100%;
    padding: 0.75rem 1rem 0.75rem 2.75rem;
    border: 1.5px solid $color-border;
    border-radius: 12px;
    font-size: $font-size-base;
    background: $color-surface;
    outline: none;
    transition: border-color 0.25s ease, box-shadow 0.25s ease;
    font-family: $font-sans;

    &:focus {
      border-color: $color-leaf-400;
      box-shadow: 0 0 0 3px rgba($color-leaf-400, 0.15);
    }

    &::placeholder {
      color: $color-text-muted;
    }
  }

  &__plant-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 0.75rem;
    max-height: 400px;
    overflow-y: auto;
    padding-right: 0.25rem;

    // custom scrollbar
    &::-webkit-scrollbar {
      width: 5px;
    }
    &::-webkit-scrollbar-track {
      background: transparent;
    }
    &::-webkit-scrollbar-thumb {
      background: $color-border;
      border-radius: 3px;
    }
  }

  &__plant-card {
    display: flex;
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding: 1rem 0.75rem;
    border: 2px solid $color-border;
    border-radius: 14px;
    background: $color-surface;
    cursor: pointer;
    transition: all 0.3s $ease-spring;

    &:hover {
      border-color: $color-leaf-300;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
    }

    &--selected {
      border-color: $color-leaf-500;
      background: $color-leaf-50;
      box-shadow: 0 0 0 3px rgba($color-leaf-500, 0.12);

      &:hover {
        border-color: $color-leaf-600;
      }
    }
  }

  &__plant-emoji {
    font-size: 2rem;
    margin-bottom: 0.375rem;
  }

  &__plant-name {
    font-weight: 600;
    font-size: $font-size-sm;
    color: $color-text;
    line-height: 1.3;
  }

  &__plant-sci {
    font-size: $font-size-xs;
    font-style: italic;
    color: $color-text-muted;
    margin-top: 0.125rem;
  }

  &__empty {
    text-align: center;
    color: $color-text-muted;
    padding: 2rem 0;
  }

  // ── Step 2 — Pot Size ─────────────────────────────────────
  &__pot-area {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2rem;
    padding: 1rem 0;
  }

  &__pot-visual {
    width: 160px;
    height: 180px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__pot-svg {
    width: 120px;
    height: 140px;
    transition: transform 0.5s $ease-spring;
    transform-origin: center bottom;
  }

  &__pot-slider-wrap {
    width: 100%;
    max-width: 420px;
  }

  &__pot-slider {
    -webkit-appearance: none;
    appearance: none;
    width: 100%;
    height: 6px;
    border-radius: 3px;
    background: linear-gradient(90deg, $color-earth-200, $color-earth-500);
    outline: none;
    cursor: pointer;

    &::-webkit-slider-thumb {
      -webkit-appearance: none;
      appearance: none;
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: $color-surface;
      border: 3px solid $color-leaf-500;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
      transition: transform 0.2s $ease-spring;
      cursor: pointer;

      &:hover {
        transform: scale(1.15);
      }
    }

    &::-moz-range-thumb {
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: $color-surface;
      border: 3px solid $color-leaf-500;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
      cursor: pointer;
    }
  }

  &__pot-labels {
    display: flex;
    justify-content: space-between;
    margin-top: 0.75rem;
  }

  &__pot-label {
    font-size: $font-size-xs;
    color: $color-text-muted;
    cursor: pointer;
    padding: 0.25rem 0.5rem;
    border-radius: 6px;
    transition: all 0.2s ease;

    &--active {
      color: $color-leaf-700;
      font-weight: 600;
      background: $color-leaf-50;
    }

    &:hover:not(&--active) {
      color: $color-text;
    }
  }

  &__pot-info {
    text-align: center;
    display: flex;
    flex-direction: column;
    gap: 0.125rem;
  }

  &__pot-info-label {
    font-family: $font-display;
    font-size: $font-size-xl;
    font-weight: 700;
    color: $color-leaf-700;
  }

  &__pot-info-range {
    font-size: $font-size-sm;
    color: $color-text-muted;
  }

  // ── Step 3 — Environment ──────────────────────────────────
  &__env-sections {
    display: flex;
    flex-direction: column;
    gap: 2rem;
  }

  &__env-group {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
  }

  &__env-label {
    font-size: $font-size-sm;
    font-weight: 600;
    color: $color-text;
    text-transform: uppercase;
    letter-spacing: 0.04em;
  }

  &__toggle-row {
    display: flex;
    gap: 0.75rem;
  }

  &__toggle-btn {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
    padding: 1.25rem 1rem;
    border: 2px solid $color-border;
    border-radius: 14px;
    background: $color-surface;
    font-size: $font-size-sm;
    font-weight: 600;
    color: $color-text-muted;
    transition: all 0.3s $ease-spring;

    &:hover {
      border-color: $color-leaf-300;
      color: $color-text;
    }

    &--active {
      border-color: $color-leaf-500;
      background: $color-leaf-50;
      color: $color-leaf-700;
      box-shadow: 0 0 0 3px rgba($color-leaf-500, 0.1);

      .watering-calc__toggle-icon {
        color: $color-leaf-600;
      }
    }
  }

  &__toggle-icon {
    width: 28px;
    height: 28px;
    color: $color-text-muted;
    transition: color 0.3s ease;
  }

  &__light-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 0.625rem;
  }

  &__light-btn {
    display: flex;
    align-items: center;
    gap: 0.625rem;
    padding: 0.875rem 1rem;
    border: 2px solid $color-border;
    border-radius: 12px;
    background: $color-surface;
    font-size: $font-size-sm;
    font-weight: 500;
    color: $color-text-muted;
    transition: all 0.3s $ease-spring;

    &:hover {
      border-color: $color-sun-300;
      color: $color-text;
    }

    &--active {
      border-color: $color-sun-400;
      background: $color-sun-50;
      color: $color-sun-700;
      box-shadow: 0 0 0 3px rgba($color-sun-400, 0.1);
    }
  }

  &__light-icon {
    font-size: 1.25rem;
  }

  &__light-text {
    font-weight: 600;
  }

  &__season-row {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 0.5rem;
  }

  &__season-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.375rem;
    padding: 0.875rem 0.5rem;
    border: 2px solid $color-border;
    border-radius: 12px;
    background: $color-surface;
    font-size: $font-size-xs;
    font-weight: 600;
    color: $color-text-muted;
    transition: all 0.3s $ease-spring;

    &:hover {
      border-color: $color-leaf-300;
      color: $color-text;
    }

    &--active {
      border-color: $color-leaf-500;
      background: $color-leaf-50;
      color: $color-leaf-700;
      box-shadow: 0 0 0 3px rgba($color-leaf-500, 0.1);
    }
  }

  &__season-icon {
    font-size: 1.5rem;
  }

  &__season-text {
    text-transform: uppercase;
    letter-spacing: 0.03em;
  }

  // ── Step 4 — Result ───────────────────────────────────────
  &__result {
    opacity: 0;
    transform: translateY(24px);
    transition: opacity 0.6s $ease-out-expo, transform 0.6s $ease-out-expo;

    &--visible {
      opacity: 1;
      transform: translateY(0);
    }
  }

  &__result-header {
    display: flex;
    align-items: center;
    gap: 1rem;
    margin-bottom: 1.5rem;
  }

  &__result-emoji {
    font-size: 3rem;
    width: 64px;
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: $color-leaf-50;
    border-radius: 16px;
    flex-shrink: 0;
  }

  &__result-plant-name {
    font-family: $font-display;
    font-size: $font-size-2xl;
    color: $color-leaf-800;
    margin-bottom: 0.125rem;
  }

  &__result-plant-sci {
    font-style: italic;
    font-size: $font-size-sm;
    color: $color-text-muted;
  }

  &__result-card {
    background: $color-surface;
    border: 1px solid $color-border;
    border-radius: 16px;
    padding: 1.25rem;
    margin-bottom: 1rem;

    &-label {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: $font-size-sm;
      font-weight: 600;
      color: $color-text;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      margin-bottom: 0.75rem;
    }

    &-icon {
      margin-bottom: 0.75rem;

      svg {
        width: 56px;
        height: 56px;
      }
    }

    &-content {
      text-align: center;
    }

    &-value {
      font-family: $font-display;
      font-size: clamp(1.5rem, 4vw, 2rem);
      font-weight: 700;
      color: $color-water-600;
      margin-bottom: 0.25rem;
    }

    &-detail {
      font-size: $font-size-sm;
      color: $color-text-muted;
    }

    &--frequency {
      text-align: center;
    }
  }

  // Calendar
  &__calendar {
    display: grid;
    grid-template-columns: repeat(7, 1fr);
    gap: 0.375rem;
    margin-bottom: 0.75rem;
  }

  &__calendar-day {
    aspect-ratio: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: $font-size-xs;
    font-weight: 500;
    border-radius: 8px;
    color: $color-text-muted;
    background: $color-bg;
    transition: all 0.3s $ease-spring;

    &--water {
      background: linear-gradient(135deg, $color-leaf-400, $color-leaf-600);
      color: white;
      font-weight: 700;
      box-shadow: 0 2px 6px rgba($color-leaf-500, 0.25);
    }
  }

  &__calendar-legend {
    display: flex;
    gap: 1rem;
    justify-content: center;
  }

  &__calendar-legend-item {
    display: flex;
    align-items: center;
    gap: 0.375rem;
    font-size: $font-size-xs;
    color: $color-text-muted;
  }

  &__calendar-legend-dot {
    width: 10px;
    height: 10px;
    border-radius: 3px;
    background: $color-bg;

    &--water {
      background: linear-gradient(135deg, $color-leaf-400, $color-leaf-600);
    }
  }

  // Tips
  &__tip-icon {
    width: 16px;
    height: 16px;
    color: $color-sun-500;
  }

  &__tips-list {
    list-style: none;
    padding: 0;
    margin: 0;
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  &__tip-item {
    font-size: $font-size-sm;
    color: $color-text;
    line-height: 1.6;
    padding-left: 1.25rem;
    position: relative;
    opacity: 0;
    animation: tipFadeIn 0.4s $ease-out-expo forwards;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0.55em;
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: $color-leaf-400;
    }
  }

  // Conditions summary tags
  &__conditions-summary {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-bottom: 1.5rem;
    justify-content: center;
  }

  &__condition-tag {
    display: inline-block;
    padding: 0.375rem 0.75rem;
    border-radius: 20px;
    font-size: $font-size-xs;
    font-weight: 600;
    background: $color-leaf-50;
    color: $color-leaf-700;
    border: 1px solid $color-leaf-200;
  }

  // Start Over
  &__start-over {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin: 0 auto;
    padding: 0.75rem 2rem;
    border: 2px solid $color-leaf-500;
    border-radius: 12px;
    background: transparent;
    color: $color-leaf-600;
    font-size: $font-size-base;
    font-weight: 600;
    transition: all 0.3s $ease-spring;

    svg {
      width: 18px;
      height: 18px;
    }

    &:hover {
      background: $color-leaf-500;
      color: white;
      transform: translateY(-2px);
      box-shadow: 0 4px 14px rgba($color-leaf-500, 0.3);
    }
  }

  // ── Navigation ────────────────────────────────────────────
  &__nav {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 2rem;
    padding-top: 1.5rem;
    border-top: 1px solid $color-border;
  }

  &__nav-btn {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1.5rem;
    border: none;
    border-radius: 12px;
    font-size: $font-size-base;
    font-weight: 600;
    transition: all 0.3s $ease-spring;

    svg {
      width: 18px;
      height: 18px;
    }

    &--back {
      background: transparent;
      color: $color-text-muted;
      border: 1.5px solid $color-border;

      &:hover {
        color: $color-text;
        border-color: $color-text-muted;
      }
    }

    &--next {
      background: linear-gradient(135deg, $color-leaf-500, $color-leaf-600);
      color: white;
      box-shadow: 0 2px 10px rgba($color-leaf-500, 0.25);

      &:hover:not(:disabled) {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba($color-leaf-500, 0.35);
      }

      &:disabled {
        opacity: 0.45;
        cursor: not-allowed;
        transform: none;
        box-shadow: none;
      }
    }
  }
}

// ── Keyframes ──────────────────────────────────────────────
@keyframes tipFadeIn {
  from {
    opacity: 0;
    transform: translateX(-8px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

// ── Responsive ─────────────────────────────────────────────
@media (max-width: 640px) {
  .watering-calc {
    padding: 1.25rem 1rem 2rem;

    &__plant-grid {
      grid-template-columns: repeat(2, 1fr);
      max-height: 320px;
    }

    &__light-grid {
      grid-template-columns: 1fr;
    }

    &__season-row {
      grid-template-columns: repeat(2, 1fr);
    }

    &__calendar-day {
      font-size: 0.65rem;
    }

    &__nav-btn {
      padding: 0.625rem 1rem;
      font-size: $font-size-sm;
    }
  }
}
</style>
