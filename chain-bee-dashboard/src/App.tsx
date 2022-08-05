import { useCallback, useRef, useState } from 'react';
import Node from './node/node';

let handleMouseMove = (e: MouseEvent) => {
  e;
};

function App() {
  const containerRef = useRef<HTMLDivElement>(null);
  const [nodePosition, setNodePosition] = useState({ x: 100, y: 100 });

  const startTrackingMouse = useCallback(
    (mousePositionOnNode: { x: number; y: number }) => {
      handleMouseMove = (event: MouseEvent) => {
        setNodePosition({
          x: event.clientX - (mousePositionOnNode.x - nodePosition.x),
          y: event.clientY - (mousePositionOnNode.y - nodePosition.y),
        });
      };
      containerRef?.current?.addEventListener('mousemove', handleMouseMove);
    },
    [nodePosition.x, nodePosition.y],
  );

  const stopTrackingMouse = useCallback(() => {
    containerRef?.current?.removeEventListener('mousemove', handleMouseMove);
  }, []);

  return (
    <div ref={containerRef} className="w-screen min-h-screen">
      <Node
        name="Test Node"
        posX={nodePosition.x}
        posY={nodePosition.y}
        onMouseDown={startTrackingMouse}
        onMouseUp={stopTrackingMouse}
      />
    </div>
  );
}

export default App;
