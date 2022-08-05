import { useRef } from 'react';

type NodeProps = {
  name: string;
  posX: number;
  posY: number;
  onMouseDown: (e: { x: number; y: number }) => void;
  onMouseUp: () => void;
};

const Node = ({ name, posX, posY, onMouseDown, onMouseUp }: NodeProps) => {
  const nodeRef = useRef<HTMLDivElement>(null);

  return (
    <div
      ref={nodeRef}
      className="relative w-[200px] h-[200px] bg-red-500 rounded-full flex justify-center items-center"
      style={{ left: `${posX}px`, top: `${posY}px` }}
      onMouseDown={(event) => {
        onMouseDown({ x: event.clientX, y: event.clientY });
      }}
      onMouseUp={onMouseUp}
    >
      <span className="h-min select-none">{name}</span>
    </div>
  );
};

export default Node;
