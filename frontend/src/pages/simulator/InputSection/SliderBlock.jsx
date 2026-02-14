import EmptyState from "./EmtyState";
import FieldBlock from "./FieldBlock";

export default function SliderBlock({ selectedGroups, blocks }) {
  // 선택된 그룹이 없으면 아무것도 보여주지 않기
  

  const visibleBlocks = blocks.filter((b) => selectedGroups.includes(b.group));
  

  return (
    <div>
      { !selectedGroups || selectedGroups.length === 0 ? (
        <EmptyState />        
      )    
      :
      (
        visibleBlocks.map((b, idx) => (
          <FieldBlock key={`${b.group}-${idx}`} title={b.title} right={b.right} />
        ))
      )}
    </div>
  );
}
