import aws = require("aws-sdk");
import { regionFromKmsKeyArn } from "@aws-crypto/kms-keyring"
import { KMS } from "aws-sdk";

const sts = new aws.STS()

export async function createGrant(KeyId: string, GranteePrincipal: string) {
  const grants = await existingGrants(KeyId, GranteePrincipal)
  
  /* If a grant exists, then we don't need to proliferate more grants.
   * It is best if this kind of operation is idempotent.
   */
  if (grants.length > 1) throw new Error(`Multiple grants exists(${grants.length}). Please delete grants to reset state.`)
  if (grants.length === 1) return grants[0]

  return await getRegionalKMS(KeyId).createGrant({
    KeyId,
    GranteePrincipal,
    RetiringPrincipal : GranteePrincipal,
    Operations: ["Decrypt", "Encrypt", "GenerateDataKey"]
  }).promise()

}

export async function existingGrants(KeyId: string, currentPrinciple: string) {

  const { Grants } = await getRegionalKMS(KeyId).listGrants({
    KeyId
  }).promise()

  if (!Grants) return []

  return Grants.filter(({GranteePrincipal}) => GranteePrincipal === currentPrinciple)
}

export async function revokeGrant(KeyId: string, currentPrinciple: string) {
  const grants = await existingGrants(KeyId, currentPrinciple)
  for (const grant of grants) {
    const { GrantId } = grant
    if (!GrantId) continue
    /* In this case we revoke instead of retire.
     * This is to make the workshop function smoothly.
     * Revoke will actively deny the operations that depend on the grant.
     */
    await getRegionalKMS(KeyId).revokeGrant({ KeyId, GrantId }).promise()
  }
}

export function getCurrentPrinciple() {
  return sts
    .getCallerIdentity()
    .promise()
    .then(({Arn}) => {
      if (!Arn) throw new Error('bad')
      return Arn
    })
}

export function getRegionalKMS(KeyId: string) {
  const region = regionFromKmsKeyArn(KeyId)
  return new KMS({ region })
}
