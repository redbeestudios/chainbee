import { useRef } from 'react';
import { useObservable } from 'react-use-observable';
import NodeContext from './node-context';

type NodeProps = {
  name: string;
  node: NodeContext;
};

const Node = ({ name, node }: NodeProps) => {
  const nodeRef = useRef<HTMLDivElement>(null);
  const [position] = useObservable(() => node.position$, [node]);

  return (
    <div
      ref={nodeRef}
      className="relative w-[200px] h-[200px] bg-red-500 rounded-full flex justify-center items-center"
      style={{ left: `${position?.x}px`, top: `${position?.y}px` }}
      onMouseDown={(event) => {
        node.onMouseDown({ x: event.clientX, y: event.clientY });
      }}
      onMouseUp={() => {
        node.onMouseUp();
      }}
    >
      <span className="h-min select-none">{name}</span>
    </div>
  );
};

export default Node;
