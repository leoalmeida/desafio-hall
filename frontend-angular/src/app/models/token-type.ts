import { Injectable } from '@angular/core';

@Injectable()
export class TokenValueType {
  id = 0;
  sub = '';
  email = '';
  name = '';
  role = '';
  permissions: string[] = [];
  iat = 123;
  exp = 456;
}

@Injectable()
export class TokenType {
  token = '';
  tokenType = '';
}