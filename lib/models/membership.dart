/// 会员订阅定价模型（对齐 LoveKey 定价：7天试用 / ¥48月 / ¥98季）
class MembershipPlan {
  final String id;
  final String name; // 计划名
  final String price; // 展示价
  final String period; // 周期
  final String tagline; // 说明
  final bool isTrial; // 是否为 7 天试用
  final bool recommended; // 是否推荐（默认选中）

  const MembershipPlan({
    required this.id,
    required this.name,
    required this.price,
    required this.period,
    required this.tagline,
    this.isTrial = false,
    this.recommended = false,
  });
}

/// 7 天免费试用期（天）
const int kTrialDays = 7;

/// 订阅计划列表（顺序即展示顺序）
const List<MembershipPlan> kMembershipPlans = [
  MembershipPlan(
    id: 'trial',
    name: '7 天免费试用',
    price: '¥0',
    period: '首 7 天',
    tagline: '之后按 ¥48/月 自动续订，可随时取消',
    isTrial: true,
    recommended: true,
  ),
  MembershipPlan(
    id: 'monthly',
    name: '月度会员',
    price: '¥48',
    period: '/月',
    tagline: '连续订阅，随时取消',
  ),
  MembershipPlan(
    id: 'quarterly',
    name: '季度会员',
    price: '¥98',
    period: '/季',
    tagline: '折合 ¥32.7/月，最划算',
  ),
];

/// 会员权益说明
const List<String> kMembershipBenefits = [
  '无限次 AI 智能回复',
  '100+ 高情商人设自由切换',
  '专属自定义人设定制',
  '恋爱开场白话术库',
  '全部恋爱教程与案例',
];
