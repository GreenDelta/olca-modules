
(* just to describe 64 bit integers. *)
type int64 = int

datatype ModelType =
  FLOW
  | PROCESS
  | PODUCT_SYSTEM

(* We do not repeat all fields here but just the ones we need for
  specifications. *)
datatype Descriptor =
  BaseDescriptor of {id: int64, refId: string}
  | CategorizedDescriptor of {id: int64, refId: string}
  | FlowDescriptor of {id: int64, refId: string}
  | ProcessDescriptor of {id: int64, refId: string}
  |
