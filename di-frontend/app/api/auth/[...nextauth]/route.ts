import NextAuth from 'next-auth';
import type { NextAuthConfig } from 'next-auth';
import type { JWT } from 'next-auth/jwt';
import type { Session } from 'next-auth';
import CredentialsProvider from 'next-auth/providers/credentials';

const config = {
  providers: [
    CredentialsProvider({
      name: 'Credentials',
      credentials: {
        email: { 
          label: "Email", 
          type: "email",
          placeholder: "example@domain.com"
        },
        password: { 
          label: "Password", 
          type: "password",
          placeholder: "••••••••"
        }
      },
      async authorize(credentials) {
        if (!credentials?.email || !credentials?.password) {
          return null;
        }
        
        try {
          // TODO: Replace with your actual authentication logic
          if (credentials.email === 'test@example.com' && credentials.password === 'password') {
            return {
              id: '1',
              email: credentials.email,
              name: 'Test User',
            };
          }
          return null;
        } catch (error) {
          console.error('Auth error:', error);
          return null;
        }
      }
    })
  ],
  callbacks: {
    async jwt({ token, user }: { token: JWT; user: any }) {
      if (user) {
        token.id = user.id;
        token.email = user.email;
      }
      return token;
    },
    async session({ session, token }: { session: Session; token: JWT }) {
      if (session.user) {
        session.user.id = token.id as string;
        session.user.email = token.email as string;
      }
      return session;
    }
  },
  pages: {
    signIn: '/login',
    error: '/auth/error',
  },
} satisfies NextAuthConfig;

const { handlers } = NextAuth(config);
export const { GET, POST } = handlers; 