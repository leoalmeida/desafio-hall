import { TokenType, TokenValueType } from './token-type';

export interface UserType {
  email: string;
  name: string;
  role: string;
  token: TokenType;
  userData?: TokenValueType;
}
