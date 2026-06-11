import { Link } from 'react-router-dom';
import TagChip from './TagChip';
import { DIFFICULTY, PLACEHOLDER_IMG } from '../utils/constants';

/**
 * 菜谱卡片：封面图 + 衬线标题 + kcal 徽标 + 难度/耗时 + 标签 chips
 * reason: 推荐理由文案（首页推荐 feed 用）
 */
export default function RecipeCard({ recipe, reason = '' }) {
  if (!recipe) return null;
  return (
    <Link
      to={`/recipe/${recipe.id}`}
      className="card overflow-hidden block transition md:hover:-translate-y-1 md:hover:shadow-lift active:scale-[0.98] animate-fade-up"
    >
      <div className="relative">
        <img
          src={recipe.coverUrl || PLACEHOLDER_IMG}
          alt={recipe.title}
          loading="lazy"
          onError={(e) => {
            e.currentTarget.src = PLACEHOLDER_IMG;
          }}
          className="w-full h-32 md:h-40 object-cover bg-ink/5"
        />
        {/* kcal 印章徽标 */}
        <span className="seal-badge absolute top-2 right-2 px-2 py-0.5 text-xs">
          {recipe.caloriesKcal ?? '--'} kcal
        </span>
        {recipe.cuisineName ? (
          <span className="absolute bottom-2 left-2 bg-ink/60 text-white text-[10px] px-2 py-0.5 rounded-md backdrop-blur-sm">
            {recipe.cuisineName}
          </span>
        ) : null}
      </div>
      <div className="p-3 space-y-1.5">
        <h3 className="font-serif font-semibold text-ink text-sm md:text-base leading-snug line-clamp-2">
          {recipe.title}
        </h3>
        <p className="text-[11px] text-mute flex items-center gap-2">
          <span>{DIFFICULTY[recipe.difficulty] || '简单'}</span>
          <span className="w-px h-2.5 bg-ink/15" />
          <span>⏱ {recipe.totalTimeMin ?? '--'} 分钟</span>
          {recipe.viewCount != null && (
            <>
              <span className="w-px h-2.5 bg-ink/15" />
              <span>👀 {recipe.viewCount}</span>
            </>
          )}
        </p>
        {recipe.tags?.length > 0 && (
          <div className="flex gap-1 overflow-hidden">
            {recipe.tags.slice(0, 2).map((t, i) => (
              <TagChip key={i} tone={i % 2 === 0 ? 'jade' : 'ochre'}>
                {t}
              </TagChip>
            ))}
          </div>
        )}
        {reason && <p className="text-[11px] text-warmth line-clamp-2">💡 {reason}</p>}
      </div>
    </Link>
  );
}
