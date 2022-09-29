import { useMemo } from 'react';
import { useObservable } from 'react-use-observable';
import NodeContext from './node-context';

type NodeProps = {
  name: string;
  node: NodeContext;
};

const Node = ({ name, node }: NodeProps) => {
  const [position] = useObservable(() => node.position$, [node.position$]);
  const [radius] = useObservable(() => node.radius$, [node.radius$]);

  const diameter = useMemo(() => (radius ?? 0) * 2, [radius]);

  return (
    <div
      className={`relative w-[200px] h-[200px] bg-red-500 rounded-full flex flex-col align-middle justify-center items-center`}
      style={{ left: `${position?.x}px`, top: `${position?.y}px` }}
      onMouseDown={(event) => {
        node.onMouseDown({ x: event.clientX, y: event.clientY });
      }}
      onMouseUp={() => {
        node.onMouseUp();
      }}
    >
      <span className="h-min select-none">{name}</span>
      <span className="h-min select-none">
        X:{position?.x} Y:{position?.y}
      </span>
    </div>
  );
};

export default Node;
